
package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

import es.deusto.sd.ecoembes.dto.CapacidadDTO;
import es.deusto.sd.ecoembes.dto.CapacidadPlasSBDTO;
import es.deusto.sd.ecoembes.gateway.PlasSBGateway;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.PlantaService;
import es.deusto.sd.ecoembes.dto.NotificacionPlasSBDTO;

@RestController
@RequestMapping("/integracion/plassb")
public class PlasSBController {

    private final PlasSBGateway plasSBGateway;
    private final PlantaService plantaService;
    private final AuthService authService;

    public PlasSBController(
            PlasSBGateway plasSBGateway,
            PlantaService plantaService,
            AuthService authService) {

        this.plasSBGateway = plasSBGateway;
        this.plantaService = plantaService;
        this.authService = authService;
    }

    @GetMapping("/capacidades")
    public ResponseEntity<List<CapacidadDTO>> consultarCapacidades(
            @RequestParam("token") Long token,
            @RequestParam(value = "fecha", required = false)
            LocalDate fecha) {

        try {

            // 1. Validar el token de Ecoembes
            authService.validarToken(token);

            List<CapacidadPlasSBDTO> capacidades =
                    new ArrayList<>();

            // 2. Consultar según se haya indicado una fecha o no
            if (fecha != null) {

                // Si hay fecha, consultar el historial completo
                capacidades = plasSBGateway.consultarTodas();

                // Filtrar las capacidades por la fecha indicada
                capacidades.removeIf(capacidad ->
                        !fecha.equals(capacidad.getFecha()));

            } else {

                // Si no hay fecha, consultar la última capacidad
                CapacidadPlasSBDTO ultima =
                        plasSBGateway.consultarUltima();

                if (ultima != null) {
                    capacidades.add(ultima);
                }
            }

            // 3. Convertir las capacidades al DTO de Ecoembes
            List<CapacidadDTO> resultado = new ArrayList<>();

            for (CapacidadPlasSBDTO capacidad : capacidades) {

                // Buscar el ID de la planta en Ecoembes
                Long idPlanta = plantaService.buscarIdPorNombre(
                        capacidad.getNombrePlanta());

                if (idPlanta != null) {

                    resultado.add(new CapacidadDTO(
                            idPlanta,
                            capacidad.getNombrePlanta(),
                            capacidad.getFecha(),
                            capacidad.getCapacidadDisponible()
                    ));
                }
            }

            return ResponseEntity.ok(resultado);

        } catch (SecurityException e) {

            return ResponseEntity.status(
                    HttpStatus.UNAUTHORIZED).build();

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().build();

        } catch (RestClientException e) {

            return ResponseEntity.status(
                    HttpStatus.BAD_GATEWAY).build();
        }
    }

    @PostMapping("/notificaciones")
    public ResponseEntity<?> enviarNotificacion(
            @RequestParam("token") Long token,
            @RequestBody NotificacionPlasSBDTO notificacion) {

        try {

            // 1. Validar el token de Ecoembes
            authService.validarToken(token);

            // 2. Enviar la notificación a PlasSB
            CapacidadPlasSBDTO capacidadActualizada =
                    plasSBGateway.enviarNotificacion(notificacion);

            // 3. Devolver la respuesta de PlasSB
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(capacidadActualizada);

        } catch (SecurityException e) {

            return ResponseEntity.status(
                    HttpStatus.UNAUTHORIZED).build();

        } catch (HttpClientErrorException.BadRequest e) {

            // PlasSB ha rechazado los datos de la notificación
            return ResponseEntity.badRequest()
                    .body(e.getResponseBodyAsString());

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (RestClientException e) {

            // No se ha podido establecer correctamente la comunicación
            return ResponseEntity.status(
                    HttpStatus.BAD_GATEWAY).build();
        }
    }
}
