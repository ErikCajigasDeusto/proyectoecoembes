package es.deusto.sd.ecoembes.dto;

public class LoginResponseDTO {

    private Long token;
    private Long idPersonal;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Long token, Long idPersonal) {
        this.token = token;
        this.idPersonal = idPersonal;
    }

    public Long getToken() {
        return token;
    }

    public void setToken(Long token) {
        this.token = token;
    }

    public Long getIdPersonal() {
        return idPersonal;
    }

    public void setIdPersonal(Long idPersonal) {
        this.idPersonal = idPersonal;
    }
}