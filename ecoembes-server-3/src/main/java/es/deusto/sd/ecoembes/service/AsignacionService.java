package es.deusto.sd.ecoembes.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.client.EcoembesClient;
import es.deusto.sd.ecoembes.dto.AsignacionDTO;
import es.deusto.sd.ecoembes.entity.Asignacion;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.Personal;
import es.deusto.sd.ecoembes.entity.PlantaReciclaje;

@Service
public class AsignacionService {

    // Almacén en memoria del servidor (Prototipo 1)
    private final Map<Long, Asignacion> asignaciones =
            new ConcurrentHashMap<>();

    private final Map<Long, PlantaReciclaje> plantas =
            new ConcurrentHashMap<>();

    private final Map<Long, Contenedor> contenedores =
            new ConcurrentHashMap<>();

    private final Map<Long, Personal> empleados =
            new ConcurrentHashMap<>();

    private final AtomicLong siguienteIdAsignacion = new AtomicLong(1L);

    // Cliente TCP hacia Servidor-ContSocket (Prototipo 2)
    private final EcoembesClient contSocketClient;

    public AsignacionService(EcoembesClient contSocketClient) {
        this.contSocketClient = contSocketClient;
    }

    // Crear una asignación
    public AsignacionDTO crearAsignacion(AsignacionDTO dto) {

        if (dto == null || dto.getIdPlanta() == null ||
                dto.getIdPersonal() == null ||
                dto.getFecha() == null ||
                dto.getIdsContenedores() == null ||
                dto.getIdsContenedores().isEmpty()) {
            throw new IllegalArgumentException(
                    "Datos de asignación incompletos"
            );
        }

        PlantaReciclaje planta = plantas.get(dto.getIdPlanta());
        Personal empleado = empleados.get(dto.getIdPersonal());

        if (planta == null || empleado == null) {
            throw new IllegalArgumentException(
                    "Planta o empleado no encontrado"
            );
        }

        List<Contenedor> seleccionados = new ArrayList<>();

        for (Long idContenedor : dto.getIdsContenedores()) {
            Contenedor contenedor = contenedores.get(idContenedor);

            if (contenedor == null) {
                throw new IllegalArgumentException(
                        "Contenedor no encontrado: " + idContenedor
                );
            }

            seleccionados.add(contenedor);
        }

        Long id = siguienteIdAsignacion.getAndIncrement();

        Asignacion asignacion = new Asignacion(
                id,
                dto.getFecha(),
                planta,
                empleado
        );

        asignacion.setContenedores(seleccionados);
        asignacion.calcularTotalEnvases();

        // Notificar a ContSocket: comprueba capacidad y descuenta de forma atómica.
        // Si no hay capacidad (o la fecha no existe) la asignación no se crea.
        try {
            boolean aceptada = contSocketClient.notificar(
                    dto.getFecha(),
                    seleccionados.size(),
                    asignacion.getTotalEnvases()
            );

            if (!aceptada) {
                throw new IllegalArgumentException(
                        "ContSocket: capacidad insuficiente o fecha no válida"
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo contactar con ContSocket: " + e.getMessage(), e
            );
        }

        asignaciones.put(id, asignacion);

        return convertirADTO(asignacion);
    }

    // Consultar la capacidad disponible (toneladas) de ContSocket en una fecha
    public double consultarCapacidad(LocalDate fecha) {
        try {
            Optional<Double> capacidad = contSocketClient.getCapacidad(fecha);

            return capacidad.orElseThrow(() -> new IllegalArgumentException(
                    "No hay capacidad registrada para la fecha " + fecha
            ));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo contactar con ContSocket: " + e.getMessage(), e
            );
        }
    }

    // Consultar una asignación por su identificador
    public AsignacionDTO consultar(Long id) {

        Asignacion asignacion = asignaciones.get(id);

        if (asignacion == null) {
            throw new IllegalArgumentException(
                    "Asignación no encontrada"
            );
        }

        return convertirADTO(asignacion);
    }

    // Consultar las asignaciones de una planta entre dos fechas
    public List<AsignacionDTO> consultarAsignaciones(
            Long idPlanta, LocalDate ini, LocalDate fin) {

        List<AsignacionDTO> resultado = new ArrayList<>();

        for (Asignacion asignacion : asignaciones.values()) {

            if (Objects.equals(asignacion.getPlanta().getId(), idPlanta)
                    && !asignacion.getFecha().isBefore(ini)
                    && !asignacion.getFecha().isAfter(fin)) {

                resultado.add(convertirADTO(asignacion));
            }
        }

        return resultado;
    }

    // Convertir entidad a DTO
    private AsignacionDTO convertirADTO(Asignacion asignacion) {

        List<Long> ids = new ArrayList<>();

        for (Contenedor contenedor : asignacion.getContenedores()) {
            ids.add(contenedor.getId());
        }

        return new AsignacionDTO(
                asignacion.getId(),
                asignacion.getPlanta().getId(),
                ids,
                asignacion.getFecha(),
                asignacion.getTotalEnvases(),
                asignacion.getPersonal().getId()
        );
    }

    // Métodos para registrar datos simulados
    public void addPlanta(PlantaReciclaje planta) {
        plantas.put(planta.getId(), planta);
    }

    public void addContenedor(Contenedor contenedor) {
        contenedores.put(contenedor.getId(), contenedor);
    }

    public void addPersonal(Personal personal) {
        empleados.put(personal.getId(), personal);
    }
}