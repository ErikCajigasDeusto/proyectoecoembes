package es.deusto.sd.ecoembes.dto;

import java.time.LocalDateTime;
import es.deusto.sd.ecoembes.entity.NivelLlenado;

public class EstadoDTO {

    private LocalDateTime fecha;
    private int numeroEnvases;
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