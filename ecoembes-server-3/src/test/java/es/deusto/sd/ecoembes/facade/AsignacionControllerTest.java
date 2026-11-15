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
import es.deusto.sd.ecoembes.service.AsignacionService;
import es.deusto.sd.ecoembes.service.AuthService;

class AsignacionControllerTest {

    private static final Long TOKEN = 123L;
    private static final Long ID = 10L;
    private static final Long ID_PLANTA = 1L;
    private static final LocalDate INI = LocalDate.of(2026, 3, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 3, 31);

    private AsignacionService asignacionService;
    private AuthService authService;
    private AsignacionController controller;

    @BeforeEach
    void setUp() {
        asignacionService = mock(AsignacionService.class);
        authService = mock(AuthService.class);
        controller = new AsignacionController(asignacionService, authService);
    }

    // ---------- Métodos auxiliares ----------

    private void tokenInvalido() {
        doThrow(new SecurityException("Token inválido")).when(authService).validarToken(TOKEN);
    }

    // ---------- crear ----------

    @Test
    void crear_datosValidos_devuelve201ConLaAsignacionCreada() {
        AsignacionDTO entrada = mock(AsignacionDTO.class);
        AsignacionDTO creada = mock(AsignacionDTO.class);
        when(asignacionService.crear(entrada)).thenReturn(creada);

        ResponseEntity<AsignacionDTO> respuesta = controller.crear(TOKEN, entrada);

        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertSame(creada, respuesta.getBody());
        verify(authService).validarToken(TOKEN);
        verify(asignacionService).crear(entrada);
    }

    @Test
    void crear_tokenInvalido_devuelve401YNoCreaNada() {
        tokenInvalido();

        ResponseEntity<AsignacionDTO> respuesta = controller.crear(TOKEN, mock(AsignacionDTO.class));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verifyNoInteractions(asignacionService);
    }

    @Test
    void crear_datosInvalidos_devuelve400SinCuerpo() {
        AsignacionDTO entrada = mock(AsignacionDTO.class);
        when(asignacionService.crear(entrada))
                .thenThrow(new IllegalArgumentException("Datos de asignación incompletos"));

        ResponseEntity<AsignacionDTO> respuesta = controller.crear(TOKEN, entrada);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void crear_otraExcepcion_sePropaga() {
        AsignacionDTO entrada = mock(AsignacionDTO.class);
        when(asignacionService.crear(entrada)).thenThrow(new RuntimeException("fallo"));

        assertThrows(RuntimeException.class, () -> controller.crear(TOKEN, entrada));
    }

    // ---------- consultar ----------

    @Test
    void consultar_existe_devuelve200ConLaAsignacion() {
        AsignacionDTO dto = mock(AsignacionDTO.class);
        when(asignacionService.consultar(ID)).thenReturn(dto);

        ResponseEntity<AsignacionDTO> respuesta = controller.consultar(TOKEN, ID);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(dto, respuesta.getBody());
        verify(authService).validarToken(TOKEN);
    }

    @Test
    void consultar_tokenInvalido_devuelve401YNoConsultaNada() {
        tokenInvalido();

        ResponseEntity<AsignacionDTO> respuesta = controller.consultar(TOKEN, ID);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verifyNoInteractions(asignacionService);
    }

    @Test
    void consultar_noExiste_devuelve404() {
        when(asignacionService.consultar(ID))
                .thenThrow(new IllegalArgumentException("Asignación no encontrada"));

        ResponseEntity<AsignacionDTO> respuesta = controller.consultar(TOKEN, ID);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    // ---------- consultarAsignaciones ----------

    @Test
    void consultarAsignaciones_datosValidos_devuelve200ConLaLista() {
        List<AsignacionDTO> lista = Arrays.asList(mock(AsignacionDTO.class), mock(AsignacionDTO.class));
        when(asignacionService.consultarAsignaciones(ID_PLANTA, INI, FIN)).thenReturn(lista);

        ResponseEntity<List<AsignacionDTO>> respuesta =
                controller.consultarAsignaciones(TOKEN, ID_PLANTA, INI, FIN);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(lista, respuesta.getBody());
        assertEquals(2, respuesta.getBody().size());
        verify(authService).validarToken(TOKEN);
        verify(asignacionService).consultarAsignaciones(ID_PLANTA, INI, FIN);
    }

    @Test
    void consultarAsignaciones_sinResultados_devuelve200ConListaVacia() {
        when(asignacionService.consultarAsignaciones(ID_PLANTA, INI, FIN))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<AsignacionDTO>> respuesta =
                controller.consultarAsignaciones(TOKEN, ID_PLANTA, INI, FIN);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().isEmpty());
    }

    @Test
    void consultarAsignaciones_tokenInvalido_devuelve401YNoConsultaNada() {
        tokenInvalido();

        ResponseEntity<List<AsignacionDTO>> respuesta =
                controller.consultarAsignaciones(TOKEN, ID_PLANTA, INI, FIN);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
        verifyNoInteractions(asignacionService);
    }

    @Test
    void consultarAsignaciones_argumentoInvalido_devuelve400() {
        when(asignacionService.consultarAsignaciones(ID_PLANTA, INI, FIN))
                .thenThrow(new IllegalArgumentException());

        ResponseEntity<List<AsignacionDTO>> respuesta =
                controller.consultarAsignaciones(TOKEN, ID_PLANTA, INI, FIN);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }
}