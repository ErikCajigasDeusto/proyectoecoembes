package es.deusto.sd.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ContenedorDTO {

    @Schema(description = "Identificador único del contenedor (opcional al crear)", example = "6")
    private Long id;

    @Schema(description = "Ubicación o dirección del contenedor", example = "Calle Ledesma 5")
    private String direccion;

    @Schema(description = "Código postal de la zona", example = "48001")
    private String codigoPostal;

    @Schema(description = "Capacidad máxima en número de envases", example = "120")
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