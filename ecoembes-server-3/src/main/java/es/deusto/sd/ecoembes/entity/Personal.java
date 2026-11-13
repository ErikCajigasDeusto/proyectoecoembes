
package es.deusto.sd.ecoembes.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;

public class Personal {

    private Long id;
    private String nombre;
    private String email;
    private String passwordHash;

    // Relación con Asignacion
    private List<Asignacion> asignaciones = new ArrayList<>();

    // Constructor sin parámetros
    public Personal() {
    }

    // Constructor con parámetros
    public Personal(Long id, String nombre,
                    String email, String password) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;

        // Guardamos el hash, no la contraseña original
        this.passwordHash = DigestUtils.sha1Hex(password);
    }

    // Comprobar si la contraseña es correcta
    public boolean comprobarPassword(String password) {
        return passwordHash != null
                && passwordHash.equals(DigestUtils.sha1Hex(password));
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // No incluimos getPasswordHash() para no exponer el hash
    public void setPassword(String password) {
        this.passwordHash = DigestUtils.sha1Hex(password);
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

        Personal other = (Personal) obj;
        return id == other.id;
    }
}
