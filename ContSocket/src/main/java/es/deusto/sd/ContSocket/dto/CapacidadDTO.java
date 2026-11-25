package es.deusto.sd.ContSocket.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public class CapacidadDTO {

	@Schema(example = "1")
	private Long idPlanta;

	@Schema(example = "2026-11-20")
	private LocalDate fecha;

	@Schema(example = "125.0")
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