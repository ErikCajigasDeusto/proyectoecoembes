package es.deusto.sd.ecoembes.dto;

public class SaturacionDTO {

    private double porcentajeSaturacion;
    private int contenedoresCriticos;
    private boolean alertaActiva;

    public SaturacionDTO() {
    }

    public SaturacionDTO(double porcentajeSaturacion,
                         int contenedoresCriticos,
                         boolean alertaActiva) {
        this.porcentajeSaturacion = porcentajeSaturacion;
        this.contenedoresCriticos = contenedoresCriticos;
        this.alertaActiva = alertaActiva;
    }

    public double getPorcentajeSaturacion() {
        return porcentajeSaturacion;
    }

    public void setPorcentajeSaturacion(double porcentajeSaturacion) {
        this.porcentajeSaturacion = porcentajeSaturacion;
    }

    public int getContenedoresCriticos() {
        return contenedoresCriticos;
    }

    public void setContenedoresCriticos(int contenedoresCriticos) {
        this.contenedoresCriticos = contenedoresCriticos;
    }

    public boolean isAlertaActiva() {
        return alertaActiva;
    }

    public void setAlertaActiva(boolean alertaActiva) {
        this.alertaActiva = alertaActiva;
    }
}