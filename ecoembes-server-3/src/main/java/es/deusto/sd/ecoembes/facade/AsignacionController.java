package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.deusto.sd.ecoembes.dto.AsignacionDTO;
import es.deusto.sd.ecoembes.service.AsignacionService;
import es.deusto.sd.ecoembes.service.AuthService;

@RestController
@RequestMapping("/asignaciones")
public class AsignacionController {

    private final AsignacionService asignacionService;
    private final AuthService authService;

    public AsignacionController(AsignacionService asignacionService,
                                AuthService authService) {
        this.asignacionService = asignacionService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<AsignacionDTO> crear(
            @RequestParam Long token,
            @RequestBody AsignacionDTO dto) {
        try {
            authService.validarToken(token);

            AsignacionDTO creada = asignacionService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsignacionDTO> consultar(
            @RequestParam Long token,
            @PathVariable Long id) {
        try {
            authService.validarToken(token);
            return ResponseEntity.ok(asignacionService.consultar(id));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<AsignacionDTO>> consultarAsignaciones(
            @RequestParam Long token,
            @RequestParam Long idPlanta,
            @RequestParam LocalDate ini,
            @RequestParam LocalDate fin) {
        try {
            authService.validarToken(token);

            return ResponseEntity.ok(
                    asignacionService.consultarAsignaciones(idPlanta, ini, fin));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
