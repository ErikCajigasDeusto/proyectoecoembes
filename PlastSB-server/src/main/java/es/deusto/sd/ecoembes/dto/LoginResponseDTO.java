package es.deusto.sd.ecoembes.dto;

public class LoginResponseDTO {

    private Long token;
    private String nombre;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Long token, String nombre) {
        this.token = token;
        this.nombre= nombre;
    }

    public Long getToken() {
        return token;
    }

    public void setToken(Long token) {
        this.token = token;
    }
    
    public String getNombre() {
		return nombre;
	}
    
    public void setNombre(String nombre) {
    			this.nombre = nombre;
    			
    }
}