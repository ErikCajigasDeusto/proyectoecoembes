package es.deusto.sd.PlastSB.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.deusto.sd.PlastSB.dto.NotificacionDTO;
import es.deusto.sd.PlastSB.entity.Capacidad;
import es.deusto.sd.PlastSB.service.CapacidadService;

@RestController
@RequestMapping("/plassb/notificaciones")
public class NotificacionController {

    private final CapacidadService capacidadService;

    public NotificacionController(CapacidadService capacidadService) {
        this.capacidadService = capacidadService;
    }

    @PostMapping
    public ResponseEntity<?> recibirNotificacion(
            @RequestBody NotificacionDTO notificacion) {

        try {
            Capacidad capacidadActualizada =
                    capacidadService.procesarNotificacion(
                            notificacion.getFecha(),
                            notificacion.getNumContenedores());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(capacidadActualizada);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
