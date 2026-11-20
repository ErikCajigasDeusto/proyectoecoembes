package es.deusto.sd.ecoembes.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import es.deusto.sd.ecoembes.entity.NivelLlenado;

public class EstadoDTO {

	@Schema(example = "2026-11-20T10:00:00")
	private LocalDateTime fecha;

	@Schema(example = "95")
	private int numeroEnvases;

	@Schema(example = "ROJO")
	private NivelLlenado nivel;

    public EstadoDTO() {
    }

    public EstadoDTO(LocalDateTime fecha, int numeroEnvases,
                     NivelLlenado nivel) {
        this.fecha = fecha;
        this.numeroEnvases = numeroEnvases;
        this.nivel = nivel;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getNumeroEnvases() {
        return numeroEnvases;
    }

    public void setNumeroEnvases(int numeroEnvases) {
        this.numeroEnvases = numeroEnvases;
    }

    public NivelLlenado getNivel() {
        return nivel;
    }

    public void setNivel(NivelLlenado nivel) {
        this.nivel = nivel;
    }
}