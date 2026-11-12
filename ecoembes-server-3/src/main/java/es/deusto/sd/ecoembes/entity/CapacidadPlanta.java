
package es.deusto.sd.ecoembes.entity;

import java.time.LocalDate;
import java.util.Objects;

public class CapacidadPlanta {

    private LocalDate fecha;
    private double toneladasDisponibles;

    // Constructor sin parámetros
    public CapacidadPlanta() {
    }

    // Constructor con parámetros
    public CapacidadPlanta(LocalDate fecha, double toneladasDisponibles) {
        this.fecha = fecha;
        this.toneladasDisponibles = toneladasDisponibles;
    }

    // Getters y setters
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

    // hashCode y equals
    @Override
    public int hashCode() {
        return Objects.hash(fecha, toneladasDisponibles);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        CapacidadPlanta other = (CapacidadPlanta) obj;

        return Objects.equals(fecha, other.fecha)
                && Double.compare(toneladasDisponibles,
                                  other.toneladasDisponibles) == 0;
    }
}
