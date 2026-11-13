
package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Asignacion {

    private Long id;
    private LocalDate fecha;
    private int totalEnvases;

    // Relaciones con otras entidades
    private PlantaReciclaje planta;
    private Personal personal;
    private List<Contenedor> contenedores = new ArrayList<>();

    // Constructor sin parámetros
    public Asignacion() {
    }

    // Constructor con parámetros
    public Asignacion(Long id, LocalDate fecha,
                     PlantaReciclaje planta, Personal personal) {
        this.id = id;
        this.fecha = fecha;
        this.planta = planta;
        this.personal = personal;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public int getTotalEnvases() {
        return totalEnvases;
    }

    public void setTotalEnvases(int totalEnvases) {
        this.totalEnvases = totalEnvases;
    }

    public PlantaReciclaje getPlanta() {
        return planta;
    }

    public void setPlanta(PlantaReciclaje planta) {
        this.planta = planta;
    }

    public Personal getPersonal() {
        return personal;
    }

    public void setPersonal(Personal personal) {
        this.personal = personal;
    }

    public List<Contenedor> getContenedores() {
        return contenedores;
    }

    public void setContenedores(List<Contenedor> contenedores) {
        this.contenedores = contenedores;
    }

    // Calcular el total de envases de la asignación
    public int calcularTotalEnvases() {
        int total = 0;

        for (Contenedor contenedor : contenedores) {
            EstadoContenedor ultimoEstado = null;

            // Buscar el último estado registrado hasta el final del día de la asignación
            for (EstadoContenedor estado : contenedor.getEstados()) {
                if (estado.getFecha() != null
                        && fecha != null
                        && !estado.getFecha().toLocalDate().isAfter(fecha)) {

                    if (ultimoEstado == null
                            || estado.getFecha().isAfter(
                                    ultimoEstado.getFecha())) {
                        ultimoEstado = estado;
                    }
                }
            }

            if (ultimoEstado != null) {
                total += ultimoEstado.getNumeroEnvases();
            }
        }

        this.totalEnvases = total;
        return total;
    }

    // hashCode y equals
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        Asignacion other = (Asignacion) obj;
        return id == other.id;
    }
}

