package es.deusto.sd.ContSocket.dao;

import java.time.LocalDate;
import java.util.Optional;

import es.deusto.sd.ContSocket.entity.Capacidad;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

public class CapacidadDAOJpa implements CapacidadDAO {

    public static final String PLANTA = "PLANTA_PRINCIPAL";

    private final EntityManagerFactory emf;

    public CapacidadDAOJpa() {
        this.emf = Persistence.createEntityManagerFactory("contsocket-pu");
    }

    private TypedQuery<Capacidad> queryPorFecha(EntityManager em, LocalDate fecha) {
        return em.createQuery(
                "SELECT c FROM Capacidad c WHERE c.fecha = :fecha AND c.nombrePlanta = :planta",
                Capacidad.class)
                .setParameter("fecha", fecha)
                .setParameter("planta", PLANTA);
    }

    @Override
    public Optional<Capacidad> findByFecha(LocalDate fecha) {
        try (EntityManager em = emf.createEntityManager()) {
            return queryPorFecha(em, fecha).getResultStream().findFirst();
        }
    }

    @Override
    public void save(Capacidad capacidad) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (capacidad.getId() == null) {
                    em.persist(capacidad);
                } else {
                    em.merge(capacidad);
                }
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean descontar(LocalDate fecha, double toneladas) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                Optional<Capacidad> opt = queryPorFecha(em, fecha)
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE) // evita carreras entre hilos
                        .getResultStream().findFirst();

                if (opt.isEmpty() || opt.get().getCapacidadDisponible() < toneladas) {
                    em.getTransaction().rollback();
                    return false;
                }
                Capacidad c = opt.get();
                // Redondeo a 3 decimales (igual que antes)
                c.setCapacidadDisponible(
                        Math.round((c.getCapacidadDisponible() - toneladas) * 1000.0) / 1000.0);
                em.getTransaction().commit();
                return true;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
        }
    }

    @Override
    public void close() {
        if (emf.isOpen()) emf.close();
    }
}