
package es.deusto.sd.ecoembes.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlantaReciclaje {

    private long id;
    private String direccion;
    private String codigoPostal;
    private String nombre;

    // Relaciones con otras entidades
    private List<CapacidadPlanta> capacidades = new ArrayList<>();
    private List<Asignacion> asignaciones = new ArrayList<>();

    // Constructor sin parámetros
    public PlantaReciclaje() {
    }

    // Constructor con parámetros
    public PlantaReciclaje(long id, String direccion,
                          String codigoPostal, String nombre) {
        this.id = id;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.nombre = nombre;
    }

    // Getters y setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<CapacidadPlanta> getCapacidades() {
        return capacidades;
    }

    public void setCapacidades(List<CapacidadPlanta> capacidades) {
        this.capacidades = capacidades;
    }

    public List<Asignacion> getAsignaciones() {
        return asignaciones;
    }

    public void setAsignaciones(List<Asignacion> asignaciones) {
        this.asignaciones = asignaciones;
    }

    // hashCode y equals
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        PlantaReciclaje other = (PlantaReciclaje) obj;
        return id == other.id;
    }
}
