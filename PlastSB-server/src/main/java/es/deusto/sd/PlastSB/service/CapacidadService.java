package es.deusto.sd.PlastSB.service;


import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.deusto.sd.PlastSB.entity.Capacidad;
import es.deusto.sd.PlastSB.repository.CapacidadRepository;

@Service
public class CapacidadService {

    private final CapacidadRepository repository;

    public CapacidadService(CapacidadRepository repository) {
        this.repository = repository;
    }

    // Consultar capacidad de una fecha
    public Capacidad consultarCapacidad(LocalDate fecha) {

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria");
        }

        return repository.findByFecha(fecha)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No existe capacidad para la fecha: "
                                        + fecha));
    }
    
    
    // Consultar la última capacidad registrada
    public Capacidad consultarUltimaCapacidad() {
        return repository.findFirstByOrderByIdDesc()
                .orElseThrow(() -> new NoSuchElementException(
                        "No existen registros de capacidad"));
    }

    // Consultar capacidades entre dos fechas opcionales
    public List<Capacidad> consultarCapacidades(
            LocalDate desde,
            LocalDate hasta) {

        // Comprobar que el rango sea válido
        if (desde != null && hasta != null
                && desde.isAfter(hasta)) {

            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // Ninguna fecha indicada: devolver todas
        if (desde == null && hasta == null) {
            return repository.findAll();
        }

        // Solo fecha inicial: desde esa fecha en adelante
        if (desde != null && hasta == null) {
            return repository.findByFechaGreaterThanEqual(desde);
        }

        // Solo fecha final: hasta esa fecha
        if (desde == null) {
            return repository.findByFechaLessThanEqual(hasta);
        }

        // Ambas fechas: devolver el intervalo
        return repository.findByFechaBetween(desde, hasta);
    }

    // Guardar una capacidad
    public Capacidad guardar(Capacidad capacidad) {

        if (capacidad == null
                || capacidad.getFecha() == null
                || capacidad.getNombrePlanta() == null
                || capacidad.getNombrePlanta().isBlank()
                || capacidad.getCapacidadTotal() < 0
                || capacidad.getCapacidadDisponible() < 0
                || capacidad.getCapacidadDisponible()
                        > capacidad.getCapacidadTotal()) {

            throw new IllegalArgumentException(
                    "Los datos de capacidad son inválidos");
        }

        return repository.save(capacidad);
    }

    // Consultar todas las capacidades
    public List<Capacidad> consultarTodas() {
        return repository.findAll();
    }
    
    
    @Transactional
    public Capacidad procesarNotificacion(
            LocalDate fecha,
            int numContenedores) {

        // 1. Validar los datos recibidos
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria");
        }

        if (numContenedores <= 0) {
            throw new IllegalArgumentException(
                    "El número de contenedores debe ser positivo");
        }

        // 2. Buscar el último registro existente
        Capacidad ultimaCapacidad = repository
                .findFirstByOrderByIdDesc()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existen registros de capacidad"));

        // 3. Comprobar que hay capacidad suficiente
        if (numContenedores >
                ultimaCapacidad.getCapacidadDisponible()) {

            throw new IllegalArgumentException(
                    "La planta no dispone de capacidad suficiente");
        }

        // 4. Restar el número de contenedores
        double nuevaCapacidadDisponible =
                ultimaCapacidad.getCapacidadDisponible()
                        - numContenedores;

        // 5. Crear un nuevo registro sin modificar el anterior
        Capacidad nuevaCapacidad = new Capacidad(
                ultimaCapacidad.getNombrePlanta(),
                fecha,
                ultimaCapacidad.getCapacidadTotal(),
                nuevaCapacidadDisponible
        );

        // 6. Guardar el nuevo registro en H2
        return repository.save(nuevaCapacidad);
    }
}