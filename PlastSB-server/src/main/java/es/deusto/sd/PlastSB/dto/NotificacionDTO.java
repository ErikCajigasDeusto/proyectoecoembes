
package es.deusto.sd.PlastSB.dto;

import java.time.LocalDate;

public class NotificacionDTO {

    private LocalDate fecha;
    private int numContenedores;

    public NotificacionDTO() {
    }

    public NotificacionDTO(
            LocalDate fecha,
            int numContenedores) {

        this.fecha = fecha;
        this.numContenedores = numContenedores;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public int getNumContenedores() {
        return numContenedores;
    }

    public void setNumContenedores(int numContenedores) {
        this.numContenedores = numContenedores;
    }
}
