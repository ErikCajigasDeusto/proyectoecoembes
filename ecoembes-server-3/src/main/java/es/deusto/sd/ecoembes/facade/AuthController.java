package es.deusto.sd.ecoembes.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.deusto.sd.ecoembes.dto.LoginRequestDTO;
import es.deusto.sd.ecoembes.dto.LoginResponseDTO;
import es.deusto.sd.ecoembes.entity.Personal;
import es.deusto.sd.ecoembes.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO dto) {

        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Long token = authService.login(dto.getEmail(), dto.getPassword());
            Personal personal = authService.validarToken(token);

            LoginResponseDTO respuesta =
                    new LoginResponseDTO(token, personal.getId());

            return ResponseEntity.ok(respuesta);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam Long token) {
        try {
            authService.validarToken(token);
            authService.logout(token);
            return ResponseEntity.noContent().build();

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
