
package es.deusto.sd.ecoembes.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class EstadoContenedor {

    private LocalDateTime fecha;
    private int numeroEnvases;
    private NivelLlenado nivel;

    // Constructor sin parámetros
    public EstadoContenedor() {
    }

    // Constructor con parámetros
    public EstadoContenedor(LocalDateTime fecha,
                            int numeroEnvases,
                            NivelLlenado nivel) {
        this.fecha = fecha;
        this.numeroEnvases = numeroEnvases;
        this.nivel = nivel;
    }

    // Getters y setters
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

    // hashCode y equals
    @Override
    public int hashCode() {
        return Objects.hash(fecha, numeroEnvases, nivel);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        EstadoContenedor other = (EstadoContenedor) obj;

        return Objects.equals(fecha, other.fecha)
                && numeroEnvases == other.numeroEnvases
                && nivel == other.nivel;
    }
}
