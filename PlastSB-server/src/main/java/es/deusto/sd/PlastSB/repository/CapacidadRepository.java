
package es.deusto.sd.PlastSB.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.deusto.sd.PlastSB.entity.Capacidad;

public interface CapacidadRepository
        extends JpaRepository<Capacidad, Long> {

    Optional<Capacidad> findByFecha(LocalDate fecha);

    List<Capacidad> findByFechaBetween(
            LocalDate desde, LocalDate hasta);

    List<Capacidad> findByFechaGreaterThanEqual(
            LocalDate desde);

    List<Capacidad> findByFechaLessThanEqual(
            LocalDate hasta);

    Optional<Capacidad> findFirstByOrderByIdDesc();
}
