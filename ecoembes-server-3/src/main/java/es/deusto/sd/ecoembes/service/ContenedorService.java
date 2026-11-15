
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

            throw new IllegalArgumentException(
                    "Datos del contenedor inválidos");
        }

        Long id;

        // Si se proporciona un ID manual, comprobamos que no exista.
        if (dto.getId() != null) {

            id = dto.getId();

            if (contenedores.containsKey(id)) {
                throw new IllegalArgumentException(
                        "Ya existe un contenedor con id: " + id);
            }

            // El siguiente ID automático será mayor que el manual.
            if (id >= siguienteIdContenedor) {
                siguienteIdContenedor = id + 1;
            }

        } else {

            // Generamos automáticamente el siguiente ID.
            id = siguienteIdContenedor++;
        }

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

        alertaService.registrarContenedor(contenedor);

        return contenedor;
    }

    public Contenedor obtenerPorId(Long id) {

        Contenedor contenedor = contenedores.get(id);

        if (contenedor == null) {
            throw new NoSuchElementException(
                    "No existe el contenedor con id: " + id);
        }

        return contenedor;
    }

    public EstadoContenedor actualizarEstado(Long id, EstadoDTO dto) {

        if (dto == null
                || dto.getNivel() == null
                || dto.getNumeroEnvases() < 0) {

            throw new IllegalArgumentException(
                    "Datos de estado inválidos");
        }

        Contenedor contenedor = contenedores.get(id);

        if (contenedor == null) {
            throw new NoSuchElementException(
                    "No existe el contenedor con id: " + id);
        }

        if (dto.getNumeroEnvases() > contenedor.getCapacidadInicial()) {
            throw new IllegalArgumentException(
                    "El número de envases supera la capacidad del contenedor");
        }

        contenedor.actualizarEstado(
                dto.getNumeroEnvases(),
                dto.getNivel());

        List<EstadoContenedor> historial = contenedor.getEstados();

        EstadoContenedor ultimoEstado =
                historial.get(historial.size() - 1);

        if (dto.getFecha() != null) {
            ultimoEstado.setFecha(dto.getFecha());
        }

        alertaService.evaluarSaturacion();

        return ultimoEstado;
    }

    public EstadoContenedor consultarEstado(Long id, LocalDate fecha) {

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha no puede ser nula");
        }

        Contenedor contenedor = contenedores.get(id);

        if (contenedor == null) {
            throw new NoSuchElementException(
                    "No existe el contenedor con id: " + id);
        }

        EstadoContenedor resultado = null;

        for (EstadoContenedor estado : contenedor.getEstados()) {

            if (estado.getFecha().toLocalDate().equals(fecha)) {

                if (resultado == null
                        || estado.getFecha().isAfter(resultado.getFecha())) {

                    resultado = estado;
                }
            }
        }

        return resultado;
    }

    public List<EstadoContenedor> consultarUso(
            Long id,
            LocalDate ini,
            LocalDate fin) {

        if (ini == null || fin == null || ini.isAfter(fin)) {
            throw new IllegalArgumentException(
                    "Rango de fechas inválido");
        }

        Contenedor contenedor = contenedores.get(id);

        if (contenedor == null) {
            throw new NoSuchElementException(
                    "No existe el contenedor con id: " + id);
        }

        List<EstadoContenedor> resultado = new ArrayList<>();

        for (EstadoContenedor estado : contenedor.getEstados()) {

            LocalDate fechaEstado = estado.getFecha().toLocalDate();

            if (!fechaEstado.isBefore(ini)
                    && !fechaEstado.isAfter(fin)) {

                resultado.add(estado);
            }
        }

        return resultado;
    }

    public List<EstadoContenedor> consultarZona(
            String codigoPostal,
            LocalDate fecha) {

        if (codigoPostal == null
                || codigoPostal.isBlank()
                || fecha == null) {

            throw new IllegalArgumentException(
                    "Código postal y fecha son obligatorios");
        }

        Zona zona = zonas.get(codigoPostal);

        if (zona == null) {
            return new ArrayList<>();
        }

        List<EstadoContenedor> resultado = new ArrayList<>();

        for (Contenedor contenedor : zona.getContenedores()) {

            EstadoContenedor estado =
                    consultarEstado(contenedor.getId(), fecha);

            if (estado != null) {
                resultado.add(estado);
            }
        }

        return resultado;
    }
}
