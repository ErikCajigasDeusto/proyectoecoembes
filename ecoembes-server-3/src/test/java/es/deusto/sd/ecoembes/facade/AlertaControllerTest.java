package es.deusto.sd.ecoembes.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.deusto.sd.ecoembes.dto.SaturacionDTO;
import es.deusto.sd.ecoembes.service.AlertaService;
import es.deusto.sd.ecoembes.service.AuthService;

class AlertaControllerTest {

    private static final Long TOKEN = 123L;

    private AlertaService alertaService;
    private AuthService authService;
    private AlertaController controller;

    @BeforeEach
    void setUp() {
        alertaService = mock(AlertaService.class);
        authService = mock(AuthService.class);
        controller = new AlertaController(alertaService, authService);
    }

    // ---------- consultarSaturacion ----------

    @Test
    void consultarSaturacion_tokenValido_devuelve200ConElDTO() {
        SaturacionDTO dto = new SaturacionDTO(50.0, 3, false);
        when(alertaService.consultarSaturacion()).thenReturn(dto);

        ResponseEntity<SaturacionDTO> respuesta = controller.consultarSaturacion(TOKEN);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(dto, respuesta.getBody());
        verify(authService).validarToken(TOKEN);
        verify(alertaService).consultarSaturacion();
    }

    @Test
    void consultarSaturacion_tokenInvalido_devuelve401SinCuerpo() {
        doThrow(new SecurityException("Token inválido")).when(authService).validarToken(TOKEN);

        ResponseEntity<SaturacionDTO> respuesta = controller.consultarSaturacion(TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void consultarSaturacion_tokenInvalido_noLlamaAlServicioDeAlertas() {
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        controller.consultarSaturacion(TOKEN);

        verifyNoInteractions(alertaService);
    }

    // ---------- evaluarSaturacion ----------

    @Test
    void evaluarSaturacion_tokenValido_devuelve200ConElDTO() {
        SaturacionDTO dto = new SaturacionDTO(90.0, 9, true);
        when(alertaService.evaluarSaturacion()).thenReturn(dto);

        ResponseEntity<SaturacionDTO> respuesta = controller.evaluarSaturacion(TOKEN);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(dto, respuesta.getBody());
        verify(authService).validarToken(TOKEN);
        verify(alertaService).evaluarSaturacion();
    }

    @Test
    void evaluarSaturacion_tokenInvalido_devuelve401SinCuerpo() {
        doThrow(new SecurityException("Token inválido")).when(authService).validarToken(TOKEN);

        ResponseEntity<SaturacionDTO> respuesta = controller.evaluarSaturacion(TOKEN);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertNull(respuesta.getBody());
    }

    @Test
    void evaluarSaturacion_tokenInvalido_noEvaluaLaSaturacion() {
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        controller.evaluarSaturacion(TOKEN);

        verify(alertaService, never()).evaluarSaturacion();
        verifyNoInteractions(alertaService);
    }
}