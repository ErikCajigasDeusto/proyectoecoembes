package es.deusto.sd.ecoembes.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.deusto.sd.ecoembes.dto.ContenedorDTO;
import es.deusto.sd.ecoembes.dto.EstadoDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.ContenedorService;

@ExtendWith(MockitoExtension.class)
public class ContenedorControllerTest {

    private static final Long TOKEN = 123L;
    private static final Long ID = 1L;
    private static final LocalDate INI = LocalDate.of(2025, 1, 1);
    private static final LocalDate FIN = LocalDate.of(2025, 1, 31);
    private static final LocalDate FECHA = LocalDate.of(2025, 1, 15);
    private static final String CP = "48001";

    @Mock
    private ContenedorService contenedorService;

    @Mock
    private AuthService authService;

    private ContenedorController controller;

    @BeforeEach
    void setUp() {
        controller = new ContenedorController(contenedorService, authService);
    }

    // Crea un EstadoContenedor mockeado válido para la conversión a DTO
    private EstadoContenedor estadoMock() {
        EstadoContenedor estado = mock(EstadoContenedor.class);
        when(estado.getFecha()).thenReturn(FECHA);
        return estado;
    }

    // ---------------------------------------------------------------
    // 1. CREAR
    // ---------------------------------------------------------------

    @Test
    void crear_ok_devuelve201() {
        ContenedorDTO dto = mock(ContenedorDTO.class);
        Contenedor contenedor = mock(Contenedor.class, RETURNS_DEEP_STUBS);
        when(contenedor.getId()).thenReturn(ID);
        when(contenedor.getDireccion()).thenReturn("Calle Mayor 1");
        when(contenedor.getZona().getCodigoPostal()).thenReturn(CP);
        when(contenedorService.crear(dto)).thenReturn(contenedor);

        ResponseEntity<ContenedorDTO> response = controller.crear(TOKEN, dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).validarToken(TOKEN);
        verify(contenedorService).crear(dto);
    }

    @Test
    void crear_tokenInvalido_devuelve401() {
        ContenedorDTO dto = mock(ContenedorDTO.class);
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        ResponseEntity<ContenedorDTO> response = controller.crear(TOKEN, dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(contenedorService);
    }

    @Test
    void crear_datosInvalidos_devuelve400() {
        ContenedorDTO dto = mock(ContenedorDTO.class);
        when(contenedorService.crear(dto)).thenThrow(new IllegalArgumentException());

        ResponseEntity<ContenedorDTO> response = controller.crear(TOKEN, dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---------------------------------------------------------------
    // 2. ACTUALIZAR ESTADO
    // ---------------------------------------------------------------

    @Test
    void actualizar_ok_devuelve200() {
        EstadoDTO dto = mock(EstadoDTO.class);
        when(contenedorService.actualizarEstado(ID, dto)).thenReturn(estadoMock());

        ResponseEntity<EstadoDTO> response = controller.actualizar(TOKEN, ID, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).validarToken(TOKEN);
    }

    @Test
    void actualizar_tokenInvalido_devuelve401() {
        EstadoDTO dto = mock(EstadoDTO.class);
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        ResponseEntity<EstadoDTO> response = controller.actualizar(TOKEN, ID, dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(contenedorService);
    }

    @Test
    void actualizar_datosInvalidos_devuelve400() {
        EstadoDTO dto = mock(EstadoDTO.class);
        when(contenedorService.actualizarEstado(ID, dto)).thenThrow(new IllegalArgumentException());

        ResponseEntity<EstadoDTO> response = controller.actualizar(TOKEN, ID, dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void actualizar_contenedorNoExiste_devuelve404() {
        EstadoDTO dto = mock(EstadoDTO.class);
        when(contenedorService.actualizarEstado(ID, dto)).thenThrow(new NoSuchElementException());

        ResponseEntity<EstadoDTO> response = controller.actualizar(TOKEN, ID, dto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---------------------------------------------------------------
    // 3. CONSULTAR USO
    // ---------------------------------------------------------------

    @Test
    void consultarUso_ok_devuelveListaConvertida() {
        when(contenedorService.consultarUso(ID, INI, FIN))
                .thenReturn(List.of(estadoMock(), estadoMock()));

        ResponseEntity<List<EstadoDTO>> response = controller.consultarUso(TOKEN, ID, INI, FIN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(contenedorService).consultarUso(ID, INI, FIN);
    }

    @Test
    void consultarUso_listaVacia_devuelve200ConListaVacia() {
        when(contenedorService.consultarUso(ID, INI, FIN)).thenReturn(List.of());

        ResponseEntity<List<EstadoDTO>> response = controller.consultarUso(TOKEN, ID, INI, FIN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void consultarUso_tokenInvalido_devuelve401() {
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        ResponseEntity<List<EstadoDTO>> response = controller.consultarUso(TOKEN, ID, INI, FIN);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(contenedorService);
    }

    @Test
    void consultarUso_fechasInvalidas_devuelve400() {
        when(contenedorService.consultarUso(ID, FIN, INI)).thenThrow(new IllegalArgumentException());

        ResponseEntity<List<EstadoDTO>> response = controller.consultarUso(TOKEN, ID, FIN, INI);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void consultarUso_contenedorNoExiste_devuelve404() {
        when(contenedorService.consultarUso(ID, INI, FIN)).thenThrow(new NoSuchElementException());

        ResponseEntity<List<EstadoDTO>> response = controller.consultarUso(TOKEN, ID, INI, FIN);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---------------------------------------------------------------
    // 4. CONSULTAR ESTADO EN UNA FECHA
    // ---------------------------------------------------------------

    @Test
    void consultarEstado_ok_devuelve200() {
        when(contenedorService.consultarEstado(ID, FECHA)).thenReturn(estadoMock());

        ResponseEntity<EstadoDTO> response = controller.consultarEstado(TOKEN, ID, FECHA);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void consultarEstado_sinEstado_devuelve404() {
        when(contenedorService.consultarEstado(ID, FECHA)).thenReturn(null);

        ResponseEntity<EstadoDTO> response = controller.consultarEstado(TOKEN, ID, FECHA);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void consultarEstado_tokenInvalido_devuelve401() {
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        ResponseEntity<EstadoDTO> response = controller.consultarEstado(TOKEN, ID, FECHA);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(contenedorService);
    }

    @Test
    void consultarEstado_argumentoInvalido_devuelve400() {
        when(contenedorService.consultarEstado(ID, FECHA)).thenThrow(new IllegalArgumentException());

        ResponseEntity<EstadoDTO> response = controller.consultarEstado(TOKEN, ID, FECHA);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void consultarEstado_contenedorNoExiste_devuelve404() {
        when(contenedorService.consultarEstado(ID, FECHA)).thenThrow(new NoSuchElementException());

        ResponseEntity<EstadoDTO> response = controller.consultarEstado(TOKEN, ID, FECHA);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---------------------------------------------------------------
    // 5. CONSULTAR ZONA
    // ---------------------------------------------------------------

    @Test
    void consultarZona_ok_devuelveListaConvertida() {
        when(contenedorService.consultarZona(CP, FECHA))
                .thenReturn(List.of(estadoMock(), estadoMock(), estadoMock()));

        ResponseEntity<List<EstadoDTO>> response = controller.consultarZona(TOKEN, CP, FECHA);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(contenedorService).consultarZona(eq(CP), eq(FECHA));
    }

    @Test
    void consultarZona_tokenInvalido_devuelve401() {
        doThrow(new SecurityException()).when(authService).validarToken(TOKEN);

        ResponseEntity<List<EstadoDTO>> response = controller.consultarZona(TOKEN, CP, FECHA);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(contenedorService);
    }

    @Test
    void consultarZona_codigoPostalInvalido_devuelve400() {
        when(contenedorService.consultarZona(any(), any())).thenThrow(new IllegalArgumentException());

        ResponseEntity<List<EstadoDTO>> response = controller.consultarZona(TOKEN, "xx", FECHA);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}