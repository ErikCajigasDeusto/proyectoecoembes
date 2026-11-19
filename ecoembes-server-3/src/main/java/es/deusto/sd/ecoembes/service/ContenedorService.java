package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.dto.ContenedorDTO;
import es.deusto.sd.ecoembes.dto.EstadoDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.Zona;

@Service
public class ContenedorService {

    private final Map<Long, Contenedor> contenedores = new HashMap<>();
    private final Map<String, Zona> zonas = new HashMap<>();

    private Long siguienteIdContenedor = 1L;
    private Long siguienteIdZona = 1L;

    private final AlertaService alertaService;

    public ContenedorService(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    public Contenedor crear(ContenedorDTO dto) {
        if (dto == null
                || dto.getCodigoPostal() == null
                || dto.getCodigoPostal().isBlank()
                || dto.getDireccion() == null
                || dto.getDireccion().isBlank()
                || dto.getCapacidadInicial() <= 0) {
            throw new IllegalArgumentException("Datos del contenedor inválidos");
        }

        Long id = siguienteIdContenedor++;
        String cp = dto.getCodigoPostal();

        Zona zona = zonas.get(cp);
        if (zona == null) {
            zona = new Zona(siguienteIdZona++, cp);
            zonas.put(cp, zona);
        }

        Contenedor contenedor = new Contenedor(
                id,
                dto.getDireccion(),
                dto.getCapacidadInicial(),
                zona);

        zona.getContenedores().add(contenedor);
        contenedores.put(id, contenedor);

        // Registra el contenedor en el servicio interno de alertas
        alertaService.registrarContenedor(contenedor);

        return contenedor;
    }

    public Contenedor obtenerPorId(Long id) {
        Contenedor contenedor = contenedores.get(id);
        if (contenedor == null) {
            throw new NoSuchElementException("No existe el contenedor con id: " + id);
        }
        return contenedor;
    }

    public EstadoContenedor actualizarEstado(Long id, EstadoDTO dto) {
        if (dto == null
                || dto.getNivel() == null
                || dto.getNumeroEnvases() < 0) {
            throw new IllegalArgumentException("Datos de estado inválidos");
        }

        Contenedor contenedor = contenedores.get(id);
        if (contenedor == null) {
            throw new NoSuchElementException("No existe el contenedor con id: " + id);
        }

        if (dto.getNumeroEnvases() > contenedor.getCapacidadInicial()) {
            throw new IllegalArgumentException("El número de envases supera la capacidad del contenedor");
        }

        contenedor.actualizarEstado(dto.getNumeroEnvases(), dto.getNivel());

        List<EstadoContenedor> historial = contenedor.getEstados();
        EstadoContenedor ultimoEstado = historial.get(historial.size() - 1);

        if (dto.getFecha() != null) {
            ultimoEstado.setFecha(dto.getFecha());
        }

        // Dispara la evaluación interna de saturación (> 80 %)
        alertaService.evaluarSaturacion();

        return ultimoEstado;
    }

    public EstadoContenedor consultarEstado(Long id, LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        Contenedor contenedor = contenedores.get(id);
        if (contenedor == null) {
            throw new NoSuchElementException("No existe el contenedor con id: " + id);
        }

        EstadoContenedor resultado = null;
        for (EstadoContenedor estado : contenedor.getEstados()) {
            if (estado.getFecha().toLocalDate().equals(fecha)) {
                if (resultado == null || estado.getFecha().isAfter(resultado.getFecha())) {
                    resultado = estado;
                }
            }
        }
        return resultado;
    }

    public List<EstadoContenedor> consultarUso(Long id, LocalDate ini, LocalDate fin) {
        if (ini != null && fin != null && ini.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        Contenedor contenedor = contenedores.get(id);
        if (contenedor == null) {
            throw new NoSuchElementException("No existe el contenedor con id: " + id);
        }

        List<EstadoContenedor> resultado = new ArrayList<>();
        for (EstadoContenedor estado : contenedor.getEstados()) {
            LocalDate fechaEstado = estado.getFecha().toLocalDate();
            boolean cumpleInicio = (ini == null) || !fechaEstado.isBefore(ini);
            boolean cumpleFin = (fin == null) || !fechaEstado.isAfter(fin);

            if (cumpleInicio && cumpleFin) {
                resultado.add(estado);
            }
        }
        return resultado;
    }

    public List<EstadoContenedor> consultarZona(String codigoPostal, LocalDate fecha) {
        List<EstadoContenedor> resultado = new ArrayList<>();
        LocalDate fechaConsulta = (fecha != null) ? fecha : LocalDate.now();

        if (codigoPostal != null && !codigoPostal.isBlank()) {
            Zona zona = zonas.get(codigoPostal);
            if (zona == null) {
                return resultado;
            }
            for (Contenedor contenedor : zona.getContenedores()) {
                EstadoContenedor estado = consultarEstado(contenedor.getId(), fechaConsulta);
                if (estado != null) {
                    resultado.add(estado);
                }
            }
        } else {
            for (Zona zona : zonas.values()) {
                for (Contenedor contenedor : zona.getContenedores()) {
                    EstadoContenedor estado = consultarEstado(contenedor.getId(), fechaConsulta);
                    if (estado != null) {
                        resultado.add(estado);
                    }
                }
            }
        }
        return resultado;
    }
}