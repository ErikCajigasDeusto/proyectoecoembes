package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.deusto.sd.ecoembes.dto.CapacidadDTO;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.PlantaService;

@RestController
@RequestMapping("/plantas")
public class PlantaController {

    private final PlantaService plantaService;
    private final AuthService authService;

    public PlantaController(PlantaService plantaService,
                            AuthService authService) {
        this.plantaService = plantaService;
        this.authService = authService;
    }

    @GetMapping("/capacidades")
    public ResponseEntity<List<CapacidadDTO>> consultarCapacidades(
            @RequestParam Long token,
            @RequestParam LocalDate fecha) {
        try {
            authService.validarToken(token);

            return ResponseEntity.ok(
                    plantaService.consultarCapacidadesDTO(fecha));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
