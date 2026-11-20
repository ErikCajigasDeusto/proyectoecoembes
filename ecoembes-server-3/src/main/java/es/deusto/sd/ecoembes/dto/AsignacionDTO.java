package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class AsignacionDTO {
	@Schema(description = "ID generado por el servidor")
	private Long id;

	@Schema(example = "1")
	private Long idPlanta;

	@Schema(example = "[1, 2, 3]")
	private List<Long> idsContenedores;

	@Schema(example = "2026-11-20")
	private LocalDate fecha;

	@Schema(example = "0")
	private int totalEnvases;

	@Schema(example = "1")
	private Long idPersonal;

	public AsignacionDTO(){
		
	}
    public AsignacionDTO(Long id, Long idPlanta,
                         List<Long> idsContenedores,
                         LocalDate fecha, int totalEnvases,
                         Long idPersonal) {
        this.id = id;
        this.idPlanta = idPlanta;
        this.idsContenedores = idsContenedores;
        this.fecha = fecha;
        this.totalEnvases = totalEnvases;
        this.idPersonal = idPersonal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPlanta() {
        return idPlanta;
    }

    public void setIdPlanta(Long idPlanta) {
        this.idPlanta = idPlanta;
    }

    public List<Long> getIdsContenedores() {
        return idsContenedores;
    }

    public void setIdsContenedores(List<Long> idsContenedores) {
        this.idsContenedores = idsContenedores;
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

    public Long getIdPersonal() {
        return idPersonal;
    }

    public void setIdPersonal(Long idPersonal) {
        this.idPersonal = idPersonal;
    }
}