package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

public class CapacidadPlasSBDTO {

    private Long id;
    private String nombrePlanta;
    private LocalDate fecha;
    private double capacidadTotal;
    private double capacidadDisponible;

    public CapacidadPlasSBDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombrePlanta() {
        return nombrePlanta;
    }

    public void setNombrePlanta(String nombrePlanta) {
        this.nombrePlanta = nombrePlanta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getCapacidadTotal() {
        return capacidadTotal;
    }

    public void setCapacidadTotal(double capacidadTotal) {
        this.capacidadTotal = capacidadTotal;
    }

    public double getCapacidadDisponible() {
        return capacidadDisponible;
    }

    public void setCapacidadDisponible(double capacidadDisponible) {
        this.capacidadDisponible = capacidadDisponible;
    }
}