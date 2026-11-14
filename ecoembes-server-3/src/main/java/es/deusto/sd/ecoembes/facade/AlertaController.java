package es.deusto.sd.ecoembes.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.deusto.sd.ecoembes.dto.SaturacionDTO;
import es.deusto.sd.ecoembes.service.AlertaService;
import es.deusto.sd.ecoembes.service.AuthService;

@RestController
@RequestMapping("/alertas/saturacion")
public class AlertaController {

    private final AlertaService alertaService;
    private final AuthService authService;

    public AlertaController(AlertaService alertaService,
                            AuthService authService) {
        this.alertaService = alertaService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<SaturacionDTO> consultarSaturacion( @RequestParam Long token) {
        try {
            authService.validarToken(token);
            return ResponseEntity.ok(alertaService.consultarSaturacion());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/evaluar")
    public ResponseEntity<SaturacionDTO> evaluarSaturacion( @RequestParam Long token) {
        try {
            authService.validarToken(token);
            return ResponseEntity.ok(alertaService.evaluarSaturacion());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}