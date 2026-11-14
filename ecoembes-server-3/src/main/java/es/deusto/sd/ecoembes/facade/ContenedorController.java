package es.deusto.sd.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.deusto.sd.ecoembes.dto.ContenedorDTO;
import es.deusto.sd.ecoembes.dto.EstadoDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.ContenedorService;

@RestController
@RequestMapping("/contenedores")
public class ContenedorController {

    private final ContenedorService contenedorService;
    private final AuthService authService;

    public ContenedorController(ContenedorService contenedorService,
                                AuthService authService) {
        this.contenedorService = contenedorService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ContenedorDTO> crear(
            @RequestParam Long token,
            @RequestBody ContenedorDTO dto) {
        try {
            authService.validarToken(token);

            Contenedor contenedor = contenedorService.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertirContenedorADTO(contenedor));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<EstadoDTO> actualizar(
            @RequestParam Long token,
            @PathVariable Long id,
            @RequestBody EstadoDTO dto) {
        try {
            authService.validarToken(token);

            EstadoContenedor estado =
                    contenedorService.actualizarEstado(id, dto);

            return ResponseEntity.ok(convertirEstadoADTO(estado));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/uso")
    public ResponseEntity<List<EstadoDTO>> consultarUso(
            @RequestParam Long token,
            @PathVariable Long id,
            @RequestParam LocalDate ini,
            @RequestParam LocalDate fin) {
        try {
            authService.validarToken(token);

            List<EstadoDTO> resultado = contenedorService
                    .consultarUso(id, ini, fin)
                    .stream()
                    .map(this::convertirEstadoADTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(resultado);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/estado")
    public ResponseEntity<EstadoDTO> consultarEstado(
            @RequestParam Long token,
            @PathVariable Long id,
            @RequestParam LocalDate fecha) {
        try {
            authService.validarToken(token);

            EstadoContenedor estado =
                    contenedorService.consultarEstado(id, fecha);

            if (estado == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(convertirEstadoADTO(estado));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/zona")
    public ResponseEntity<List<EstadoDTO>> consultarZona(
            @RequestParam Long token,
            @RequestParam String cp,
            @RequestParam LocalDate fecha) {
        try {
            authService.validarToken(token);

            List<EstadoDTO> resultado = contenedorService
                    .consultarZona(cp, fecha)
                    .stream()
                    .map(this::convertirEstadoADTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(resultado);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ContenedorDTO convertirContenedorADTO(Contenedor contenedor) {
        return new ContenedorDTO(
                contenedor.getId(),
                contenedor.getDireccion(),
                contenedor.getZona().getCodigoPostal(),
                contenedor.getCapacidadInicial()
        );
    }

    private EstadoDTO convertirEstadoADTO(EstadoContenedor estado) {
        return new EstadoDTO(
                estado.getFecha(),
                estado.getNumeroEnvases(),
                estado.getNivel()
        );
    }
}
