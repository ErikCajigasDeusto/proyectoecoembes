package es.deusto.sd.ecoembes.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.deusto.sd.ecoembes.dto.CapacidadDTO;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.PlantaService;

class PlantaControllerTest {

    private static final Long TOKEN = 123L;
    private static final LocalDate FECHA = LocalDate.of(2026, 3, 15);

    private PlantaService plantaService;
    private AuthService authService;
    private PlantaController controller;

    @BeforeEach
    void setUp() {
        plantaService = mock(PlantaService.class);
        authService = mock(AuthService.class);
        controller = new PlantaController(plantaService, authService);
    }

    // ---------- consultarCapacidades ----------

    @Test
    void consultarCapacidades_tokenValido_devuelve200ConLaLista() {
        List<CapacidadDTO> capacidades = Arrays.asList(
                new CapacidadDTO(1L, FECHA, 100.0),
                new CapacidadDTO(2L, FECHA, 200.0));
        when(plantaService.consultarCapacidadesDTO(FECHA)).thenReturn(capacidades);

        ResponseEntity<List<CapacidadDTO>> respuesta = controller.consultarCapacidades(TOKEN, FECHA);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(capacidades, respuesta.getBody());
        assertEquals(2, respuesta.getBody().size());
        verify(authService).validarToken(TOKEN);
        verify(plantaService).consultarCapacidadesDTO(FECHA);
    }

    @Test
    void consultarCapacidades_sinResultados_devuelve200ConListaVacia() {
        when(plantaService.consultarCapacidadesDTO(FECHA)).thenReturn(Collections.emptyList());

        ResponseEntity<List<CapacidadDTO>> respuesta = controller.consultarCapacidades(TOKEN, FECHA);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().isEmpty());
    }

    @Test
    void consultarCapacidades_tokenInvalido_devuelve401SinCuerpo() {
        doThrow(new SecurityException("Token inválido")).when(authService).validarToken(TOKEN);

        ResponseEntity<List<CapacidadDTO>> respuesta = controller.consultarCapacidades(TOKEN, FECHA);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void consultarCapacidades_tokenInvalido_noConsultaElServicioDePlantas() {
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        controller.consultarCapacidades(TOKEN, FECHA);

        verifyNoInteractions(plantaService);
    }

    @Test
    void consultarCapacidades_fechaInvalida_devuelve400SinCuerpo() {
        when(plantaService.consultarCapacidadesDTO(null))
                .thenThrow(new IllegalArgumentException("La fecha no puede ser nula"));

        ResponseEntity<List<CapacidadDTO>> respuesta = controller.consultarCapacidades(TOKEN, null);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void consultarCapacidades_otraExcepcion_sePropaga() {
        when(plantaService.consultarCapacidadesDTO(FECHA)).thenThrow(new RuntimeException("fallo"));

        assertThrows(RuntimeException.class,
                () -> controller.consultarCapacidades(TOKEN, FECHA));
    }
}