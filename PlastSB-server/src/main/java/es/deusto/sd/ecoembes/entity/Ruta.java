
package es.deusto.sd.ecoembes.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ruta {

    private Long id;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private double duracionEstimada;
    private double distanciaKm;

    // Relaciones con otras entidades
    private Camion camion;
    private List<Contenedor> contenedores = new ArrayList<>();

    // Constructor sin parámetros
    public Ruta() {
    }

    // Constructor con parámetros
    public Ruta(Long id, LocalDateTime fechaInicio,
                LocalDateTime fechaFin, double duracionEstimada,
                double distanciaKm, Camion camion) {
        this.id = id;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.duracionEstimada = duracionEstimada;
        this.distanciaKm = distanciaKm;
        this.camion = camion;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public double getDuracionEstimada() {
        return duracionEstimada;
    }

    public void setDuracionEstimada(double duracionEstimada) {
        this.duracionEstimada = duracionEstimada;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public Camion getCamion() {
        return camion;
    }

    public void setCamion(Camion camion) {
        this.camion = camion;
    }

    public List<Contenedor> getContenedores() {
        return contenedores;
    }

    public void setContenedores(List<Contenedor> contenedores) {
        this.contenedores = contenedores;
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

        Ruta other = (Ruta) obj;
        return id == other.id;
    }
}
