package es.deusto.sd.ecoembes.dto;

import java.time.LocalDate;

public class NotificacionPlasSBDTO {

    private LocalDate fecha;
    private int numContenedores;

    public NotificacionPlasSBDTO() {
    }

    public NotificacionPlasSBDTO(
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