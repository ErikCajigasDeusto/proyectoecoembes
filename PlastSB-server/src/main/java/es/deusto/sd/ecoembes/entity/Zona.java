
package es.deusto.sd.ecoembes.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Zona {

    private Long id;
    private String codigoPostal;
    private List<Contenedor> contenedores = new ArrayList<>();

    // Constructor sin parámetros
    public Zona() {
    }

    // Constructor con parámetros
    public Zona(Long id, String codigoPostal) {
        this.id = id;
        this.codigoPostal = codigoPostal;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public List<Contenedor> getContenedores() {
        return contenedores;
    }

    public void setContenedores(List<Contenedor> contenedores) {
        this.contenedores = contenedores;
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
        if (!(obj instanceof Zona))
            return false;

        Zona other = (Zona) obj;
        return Objects.equals(id, other.id);
    }
}
