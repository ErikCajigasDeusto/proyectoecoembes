
package es.deusto.sd.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public class CapacidadDTO {

    @Schema(example = "1")
    private Long idPlanta;

    @Schema(example = "Planta de reciclaje Bilbao")
    private String nombrePlanta;

    @Schema(example = "2026-11-20")
    private LocalDate fecha;

    @Schema(example = "125.0")
    private double toneladasDisponibles;

    public CapacidadDTO() {
    }

    public CapacidadDTO(
            Long idPlanta,
            String nombrePlanta,
            LocalDate fecha,
            double toneladasDisponibles) {

        this.idPlanta = idPlanta;
        this.nombrePlanta = nombrePlanta;
        this.fecha = fecha;
        this.toneladasDisponibles = toneladasDisponibles;
    }

    public Long getIdPlanta() {
        return idPlanta;
    }

    public void setIdPlanta(Long idPlanta) {
        this.idPlanta = idPlanta;
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

    public double getToneladasDisponibles() {
        return toneladasDisponibles;
    }

    public void setToneladasDisponibles(double toneladasDisponibles) {
        this.toneladasDisponibles = toneladasDisponibles;
    }
}
