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

    // Fuente única de verdad en memoria (Prototipo 1)
    private final Map<Long, Contenedor> contenedores = new HashMap<>();
    private final Map<String, Zona> zonas = new HashMap<>();

    private Long siguienteIdContenedor = 1L;
    private Long siguienteIdZona = 1L;

    private final AlertaService alertaService;

    public ContenedorService(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // 1. CREAR UN CONTENEDOR
    public Contenedor crear(ContenedorDTO dto) {
        // Comprobaciones de validez de los datos del DTO
    		if (dto == null || dto.getCodigoPostal() == null || dto.getCodigoPostal().isBlank()
                || dto.getDireccion() == null || dto.getDireccion().isBlank()
                || dto.getCapacidadInicial() <= 0) {
            throw new IllegalArgumentException("Datos del contenedor inválidos");
        }

        String cp = dto.getCodigoPostal();
        Zona zona = zonas.get(cp);
        
        if (zona == null) {
            zona = new Zona(siguienteIdZona++, cp);
            zonas.put(cp, zona);
        }

        Long id = (dto.getId() != null) ? dto.getId() : siguienteIdContenedor++;
        Contenedor contenedor = new Contenedor(id, dto.getDireccion(), dto.getCapacidadInicial(), zona);

        zona.getContenedores().add(contenedor);
        contenedores.put(id, contenedor);

        // Registrar en el monitor global de saturación
        alertaService.registrarContenedor(contenedor);

        return contenedor;
    }

    // 2. ACTUALIZAR EL ESTADO DE UN CONTENEDOR
    public EstadoContenedor actualizarEstado(Long id, EstadoDTO dto) {
        // Comprobaciones de validez de los datos del DTO
    		if (dto == null || dto.getNivel() == null || dto.getNumeroEnvases() < 0) {
            throw new IllegalArgumentException("Datos de estado inválidos");
        }
    		
    		// Comprobación de existencia del contenedor
        Contenedor contenedor = contenedores.get(id);
        if (contenedor == null) {
            throw new NoSuchElementException("No existe ningún contenedor con id: " + id);
        }
        
        // Comprobación de que el número de envases no supera la capacidad del contenedor
        if (dto.getNumeroEnvases() > contenedor.getCapacidadInicial()) {
            throw new IllegalArgumentException("El número de envases supera la capacidad del contenedor");
        }

        // Única fuente de verdad: el historial interno de la entidad Contenedor
        contenedor.actualizarEstado(dto.getNumeroEnvases(), dto.getNivel());

        List<EstadoContenedor> historial = contenedor.getEstados();
        EstadoContenedor ultimoEstado = historial.get(historial.size() - 1);
        if (dto.getFecha() != null) {
            ultimoEstado.setFecha(dto.getFecha());
        }

        // Evaluar saturación global tras la actualización
        alertaService.evaluarSaturacion();

        return ultimoEstado;
    }

    // 3. CONSULTAR EL USO DE UN CONTENEDOR ENTRE DOS FECHAS
    public List<EstadoContenedor> consultarUso(Long id, LocalDate ini, LocalDate fin) {
        // Comprobaciones de validez de las fechas
    		if (ini == null || fin == null || ini.isAfter(fin)) {
            throw new IllegalArgumentException("Rango de fechas inválido");
        }

        Contenedor contenedor = contenedores.get(id);
        // Comprobación de existencia del contenedor
        if (contenedor == null) {
            throw new NoSuchElementException("No existe ningún contenedor con id: " + id);
        }

        List<EstadoContenedor> resultado = new ArrayList<>();
        for (EstadoContenedor estado : contenedor.getEstados()) {
            LocalDate fechaEstado = estado.getFecha().toLocalDate();
            if (!fechaEstado.isBefore(ini) && !fechaEstado.isAfter(fin)) {
                resultado.add(estado);
            }
        }
        return resultado;
    }

    // 4. CONSULTAR EL ESTADO MÁS RECIENTE DE UN CONTENEDOR EN UNA FECHA
    public EstadoContenedor consultarEstado(Long id, LocalDate fecha) {
        // Comprobación de validez de la fecha
    		if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        Contenedor contenedor = contenedores.get(id);
        // Comprobación de existencia del contenedor
        if (contenedor == null) {
            throw new NoSuchElementException("No existe ningún contenedor con id: " + id);
        }

        EstadoContenedor ultimoDelDia = null;
        for (EstadoContenedor estado : contenedor.getEstados()) {
            if (estado.getFecha().toLocalDate().equals(fecha)) {
                if (ultimoDelDia == null || estado.getFecha().isAfter(ultimoDelDia.getFecha())) {
                    ultimoDelDia = estado;
                }
            }
        }
        return ultimoDelDia;
    }

    // 5. CONSULTAR LOS ESTADOS DE UNA ZONA EN UNA FECHA
    public List<EstadoContenedor> consultarZona(String cp, LocalDate fecha) {
        // Comprobación de validez de los parámetros
    		if (cp == null || fecha == null) {
            throw new IllegalArgumentException("Código postal y fecha son obligatorios");
        }

        List<EstadoContenedor> resultado = new ArrayList<>();
        Zona zona = zonas.get(cp);
        if (zona == null) {
            return resultado;
        }

        for (Contenedor contenedor : zona.getContenedores()) {
            EstadoContenedor estadoEnFecha = consultarEstado(contenedor.getId(), fecha);
            if (estadoEnFecha != null) {
                resultado.add(estadoEnFecha);
            }
        }
        return resultado;
    }

    // Método auxiliar para que AsignacionService recupere un contenedor por ID
    public Contenedor obtenerPorId(Long id) {
        return contenedores.get(id);
    }
}