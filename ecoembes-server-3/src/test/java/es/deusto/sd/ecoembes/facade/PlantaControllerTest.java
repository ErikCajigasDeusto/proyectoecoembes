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

import es.deusto.sd.ecoembes.dto.AsignacionDTO;
import es.deusto.sd.ecoembes.dto.CapacidadDTO;
import es.deusto.sd.ecoembes.service.AsignacionService;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.PlantaService;

class PlantaControllerTest {

    private static final Long TOKEN = 123L;
    private static final LocalDate FECHA = LocalDate.of(2026, 3, 15);
    private static final LocalDate INI = LocalDate.of(2026, 3, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 3, 31);

    private PlantaService plantaService;
    private AsignacionService asignacionService;
    private AuthService authService;
    private PlantaController controller;

    @BeforeEach
    void setUp() {
        plantaService = mock(PlantaService.class);
        asignacionService = mock(AsignacionService.class);
        authService = mock(AuthService.class);
        controller = new PlantaController(plantaService, asignacionService, authService);
    }

    // =====================================================
    // consultarCapacidades
    // =====================================================

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

    // =====================================================
    // crearAsignacion
    // =====================================================

    @Test
    void crearAsignacion_tokenValido_devuelve201ConLaAsignacionCreada() {
        AsignacionDTO entrada = mock(AsignacionDTO.class);
        AsignacionDTO creada = mock(AsignacionDTO.class);
        when(asignacionService.crearAsignacion(entrada)).thenReturn(creada);

        ResponseEntity<AsignacionDTO> respuesta = controller.crearAsignacion(TOKEN, entrada);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(creada, respuesta.getBody());
        verify(authService).validarToken(TOKEN);
        verify(asignacionService).crearAsignacion(entrada);
    }

    @Test
    void crearAsignacion_tokenInvalido_devuelve401SinCuerpo() {
        AsignacionDTO entrada = mock(AsignacionDTO.class);
        doThrow(new SecurityException("Token inválido")).when(authService).validarToken(TOKEN);

        ResponseEntity<AsignacionDTO> respuesta = controller.crearAsignacion(TOKEN, entrada);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void crearAsignacion_tokenInvalido_noLlamaAlServicioDeAsignaciones() {
        AsignacionDTO entrada = mock(AsignacionDTO.class);
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        controller.crearAsignacion(TOKEN, entrada);

        verifyNoInteractions(asignacionService);
    }

    @Test
    void crearAsignacion_datosInvalidos_devuelve400SinCuerpo() {
        AsignacionDTO entrada = mock(AsignacionDTO.class);
        when(asignacionService.crearAsignacion(entrada))
                .thenThrow(new IllegalArgumentException("Datos inválidos"));

        ResponseEntity<AsignacionDTO> respuesta = controller.crearAsignacion(TOKEN, entrada);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void crearAsignacion_otraExcepcion_sePropaga() {
        AsignacionDTO entrada = mock(AsignacionDTO.class);
        when(asignacionService.crearAsignacion(entrada)).thenThrow(new RuntimeException("fallo"));

        assertThrows(RuntimeException.class,
                () -> controller.crearAsignacion(TOKEN, entrada));
    }

    // =====================================================
    // consultarAsignacion
    // =====================================================

    @Test
    void consultarAsignacion_existente_devuelve200ConLaAsignacion() {
        AsignacionDTO asignacion = mock(AsignacionDTO.class);
        when(asignacionService.consultar(1L)).thenReturn(asignacion);

        ResponseEntity<AsignacionDTO> respuesta = controller.consultarAsignacion(TOKEN, 1L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(asignacion, respuesta.getBody());
        verify(authService).validarToken(TOKEN);
        verify(asignacionService).consultar(1L);
    }

    @Test
    void consultarAsignacion_tokenInvalido_devuelve401SinCuerpo() {
        doThrow(new SecurityException("Token inválido")).when(authService).validarToken(TOKEN);

        ResponseEntity<AsignacionDTO> respuesta = controller.consultarAsignacion(TOKEN, 1L);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verifyNoInteractions(asignacionService);
    }

    @Test
    void consultarAsignacion_noExiste_devuelve404SinCuerpo() {
        when(asignacionService.consultar(99L))
                .thenThrow(new IllegalArgumentException("No existe"));

        ResponseEntity<AsignacionDTO> respuesta = controller.consultarAsignacion(TOKEN, 99L);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void consultarAsignacion_otraExcepcion_sePropaga() {
        when(asignacionService.consultar(1L)).thenThrow(new RuntimeException("fallo"));

        assertThrows(RuntimeException.class,
                () -> controller.consultarAsignacion(TOKEN, 1L));
    }

    // =====================================================
    // consultarAsignaciones
    // =====================================================

    @Test
    void consultarAsignaciones_tokenValido_devuelve200ConLaLista() {
        List<AsignacionDTO> lista = Arrays.asList(
                mock(AsignacionDTO.class),
                mock(AsignacionDTO.class));
        when(asignacionService.consultarAsignaciones(1L, INI, FIN)).thenReturn(lista);

        ResponseEntity<List<AsignacionDTO>> respuesta =
                controller.consultarAsignaciones(TOKEN, 1L, INI, FIN);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(lista, respuesta.getBody());
        assertEquals(2, respuesta.getBody().size());
        verify(authService).validarToken(TOKEN);
        verify(asignacionService).consultarAsignaciones(1L, INI, FIN);
    }

    @Test
    void consultarAsignaciones_sinResultados_devuelve200ConListaVacia() {
        when(asignacionService.consultarAsignaciones(1L, INI, FIN))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<AsignacionDTO>> respuesta =
                controller.consultarAsignaciones(TOKEN, 1L, INI, FIN);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().isEmpty());
    }

    @Test
    void consultarAsignaciones_tokenInvalido_devuelve401SinCuerpo() {
        doThrow(new SecurityException("Token inválido")).when(authService).validarToken(TOKEN);

        ResponseEntity<List<AsignacionDTO>> respuesta =
                controller.consultarAsignaciones(TOKEN, 1L, INI, FIN);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verifyNoInteractions(asignacionService);
    }

    @Test
    void consultarAsignaciones_parametrosInvalidos_devuelve400SinCuerpo() {
        when(asignacionService.consultarAsignaciones(1L, FIN, INI))
                .thenThrow(new IllegalArgumentException("Rango de fechas inválido"));

        ResponseEntity<List<AsignacionDTO>> respuesta =
                controller.consultarAsignaciones(TOKEN, 1L, FIN, INI);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void consultarAsignaciones_otraExcepcion_sePropaga() {
        when(asignacionService.consultarAsignaciones(1L, INI, FIN))
                .thenThrow(new RuntimeException("fallo"));

        assertThrows(RuntimeException.class,
                () -> controller.consultarAsignaciones(TOKEN, 1L, INI, FIN));
    }
}