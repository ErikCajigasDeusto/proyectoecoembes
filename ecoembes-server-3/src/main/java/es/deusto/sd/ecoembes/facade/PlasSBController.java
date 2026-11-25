package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import es.deusto.sd.ecoembes.dto.CapacidadDTO;
import es.deusto.sd.ecoembes.dto.CapacidadPlasSBDTO;
import es.deusto.sd.ecoembes.gateway.PlasSBGateway;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.PlantaService;

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
            @RequestParam("fecha") LocalDate fecha) {

        try {
            // 1. Validar el token de Ecoembes
            authService.validarToken(token);

            // 2. Consultar las capacidades de PlasSB
            List<CapacidadPlasSBDTO> capacidades =
                    plasSBGateway.consultarTodas();

            List<CapacidadDTO> resultado = new ArrayList<>();

            // 3. Filtrar las capacidades por fecha
            for (CapacidadPlasSBDTO capacidad : capacidades) {

                if (fecha.equals(capacidad.getFecha())) {

                    // 4. Buscar el ID de la planta en Ecoembes
                    Long idPlanta = plantaService.buscarIdPorNombre(
                            capacidad.getNombrePlanta());

                    if (idPlanta != null) {

                        // 5. Convertir al DTO de Ecoembes
                        resultado.add(new CapacidadDTO(
                                idPlanta,
                                capacidad.getFecha(),
                                capacidad.getCapacidadDisponible()
                        ));
                    }
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
}