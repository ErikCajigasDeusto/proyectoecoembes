package es.deusto.sd.ecoembes.dto;

public class ContenedorDTO {

    private Long id;
    private String direccion;
    private String codigoPostal;
    private int capacidadInicial;

    public ContenedorDTO() {
    }

    public ContenedorDTO(Long id, String direccion,
                         String codigoPostal, int capacidadInicial) {
        this.id = id;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.capacidadInicial = capacidadInicial;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public int getCapacidadInicial() {
        return capacidadInicial;
    }

    public void setCapacidadInicial(int capacidadInicial) {
        this.capacidadInicial = capacidadInicial;
    }
}