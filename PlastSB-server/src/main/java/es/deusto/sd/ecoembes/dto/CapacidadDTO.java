package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

public class CapacidadDTO {

    private Long idPlanta;
    private LocalDate fecha;
    private double toneladasDisponibles;

    public CapacidadDTO() {
    }

    public CapacidadDTO(Long idPlanta, LocalDate fecha,
                        double toneladasDisponibles) {
        this.idPlanta = idPlanta;
        this.fecha = fecha;
        this.toneladasDisponibles = toneladasDisponibles;
    }

    public Long getIdPlanta() {
        return idPlanta;
    }

    public void setIdPlanta(Long idPlanta) {
        this.idPlanta = idPlanta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getToneladasDisponibles() {
        return toneladasDisponibles;
    }

    public void setToneladasDisponibles(double toneladasDisponibles) {
        this.toneladasDisponibles = toneladasDisponibles;
    }
}