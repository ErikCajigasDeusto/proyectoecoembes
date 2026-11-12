
package es.deusto.sd.ecoembes.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Contenedor {

    private long id;
    private String direccion;
    private int capacidadInicial;

    // Relaciones con otras entidades
    private Zona zona;
    private List<EstadoContenedor> estados = new ArrayList<>();

    // Constructor sin parámetros
    public Contenedor() {
    }

    // Constructor con parámetros
    public Contenedor(long id, String direccion,
                     int capacidadInicial, Zona zona) {
        this.id = id;
        this.direccion = direccion;
        this.capacidadInicial = capacidadInicial;
        this.zona = zona;
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

    public int getCapacidadInicial() {
        return capacidadInicial;
    }

    public void setCapacidadInicial(int capacidadInicial) {
        this.capacidadInicial = capacidadInicial;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public List<EstadoContenedor> getEstados() {
        return estados;
    }

    public void setEstados(List<EstadoContenedor> estados) {
        this.estados = estados;
    }

    // Registrar un nuevo estado
    public void actualizarEstado(EstadoContenedor estado) {
        estados.add(estado);
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

        Contenedor other = (Contenedor) obj;
        return id == other.id;
    }
}
