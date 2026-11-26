package es.deusto.sd.ContSocket.dao;

import java.time.LocalDate;
import java.util.Optional;

import es.deusto.sd.ContSocket.entity.Capacidad;

public interface CapacidadDAO {

    Optional<Capacidad> findByFecha(LocalDate fecha);

    void save(Capacidad capacidad);

    /**
     * Descuenta toneladas de forma atómica (transacción + bloqueo).
     * @return true si la fecha existe y había capacidad suficiente.
     */
    boolean descontar(LocalDate fecha, double toneladas);

    void close();
}