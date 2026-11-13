
package es.deusto.sd.ecoembes.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Camion {

    private Long id;
    private String matricula;
    private double capacidadCarga;

    // Relación con Ruta
    private List<Ruta> rutas = new ArrayList<>();

    // Constructor sin parámetros
    public Camion() {
    }

    // Constructor con parámetros
    public Camion(Long id, String matricula, double capacidadCarga) {
        this.id = id;
        this.matricula = matricula;
        this.capacidadCarga = capacidadCarga;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public double getCapacidadCarga() {
        return capacidadCarga;
    }

    public void setCapacidadCarga(double capacidadCarga) {
        this.capacidadCarga = capacidadCarga;
    }

    public List<Ruta> getRutas() {
        return rutas;
    }

    public void setRutas(List<Ruta> rutas) {
        this.rutas = rutas;
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

        Camion other = (Camion) obj;
        return id == other.id;
    }
}

