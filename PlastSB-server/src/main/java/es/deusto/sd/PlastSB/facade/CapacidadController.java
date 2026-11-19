package es.deusto.sd.PlastSB.facade;


import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.deusto.sd.PlastSB.entity.Capacidad;
import es.deusto.sd.PlastSB.service.CapacidadService;

@RestController
@RequestMapping("/api/capacidades")
public class CapacidadController {

    private final CapacidadService service;

    public CapacidadController(CapacidadService service) {
        this.service = service;
    }

    // Consultar capacidad de una fecha concreta
    @GetMapping("/{fecha}")
    public ResponseEntity<Capacidad> consultar(
            @PathVariable("fecha")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        try {
            return ResponseEntity.ok(
                    service.consultarCapacidad(fecha));

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Consultar capacidades entre dos fechas opcionales
    @GetMapping("/rango")
    public ResponseEntity<List<Capacidad>> consultarRango(

            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,

            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta) {

        try {
            return ResponseEntity.ok(
                    service.consultarCapacidades(desde, hasta));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Consultar todas las capacidades
    @GetMapping
    public ResponseEntity<List<Capacidad>> consultarTodas() {
        return ResponseEntity.ok(
                service.consultarTodas());
    }

    // Guardar una capacidad
    @PostMapping
    public ResponseEntity<Capacidad> guardar(
            @RequestBody Capacidad capacidad) {

        try {
            Capacidad guardada = service.guardar(capacidad);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(guardada);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
