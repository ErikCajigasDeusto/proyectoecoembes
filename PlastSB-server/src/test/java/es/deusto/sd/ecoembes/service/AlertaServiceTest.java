package es.deusto.sd.ecoembes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.dto.SaturacionDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.NivelLlenado;

class AlertaServiceTest {

    private static final String MENSAJE_ALERTA = "[ALERTA SATURACIÓN]";

    private AlertaService alertaService;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream salida;

    @BeforeEach
    void setUp() {
        alertaService = new AlertaService();
        salida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salida));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ---------- Métodos auxiliares ----------

    /** Crea un contenedor simulado cuyo historial tiene los niveles indicados, en orden. */
    private Contenedor crearContenedor(Long id, NivelLlenado... niveles) {
        List<EstadoContenedor> historial = new ArrayList<>();
        for (NivelLlenado nivel : niveles) {
            EstadoContenedor estado = mock(EstadoContenedor.class);
            when(estado.getNivel()).thenReturn(nivel);
            historial.add(estado);
        }
        return crearContenedorConHistorial(id, historial);
    }

    private Contenedor crearContenedorConHistorial(Long id, List<EstadoContenedor> historial) {
        Contenedor contenedor = mock(Contenedor.class);
        when(contenedor.getId()).thenReturn(id);
        when(contenedor.getEstados()).thenReturn(historial);
        return contenedor;
    }

    /** Registra n contenedores en NARANJA y m en VERDE, con ids consecutivos. */
    private void registrarContenedores(int naranjas, int verdes) {
        long id = 1;
        for (int i = 0; i < naranjas; i++) {
            alertaService.registrarContenedor(crearContenedor(id++, NivelLlenado.NARANJA));
        }
        for (int i = 0; i < verdes; i++) {
            alertaService.registrarContenedor(crearContenedor(id++, NivelLlenado.VERDE));
        }
    }

    private int contarAlertasImpresas() {
        String texto = salida.toString();
        int count = 0;
        int idx = 0;
        while ((idx = texto.indexOf(MENSAJE_ALERTA, idx)) != -1) {
            count++;
            idx += MENSAJE_ALERTA.length();
        }
        return count;
    }

    // ---------- consultarSaturacion ----------

    @Test
    void consultarSaturacion_estadoInicial_esCeroYSinAlerta() {
        SaturacionDTO dto = alertaService.consultarSaturacion();

        assertNotNull(dto);
        assertFalse(dto.isAlertaActiva());
    }

    @Test
    void consultarSaturacion_devuelveElUltimoEstadoEvaluado() {
        registrarContenedores(5, 0);

        SaturacionDTO evaluado = alertaService.evaluarSaturacion();
        SaturacionDTO consultado = alertaService.consultarSaturacion();

        assertSame(evaluado, consultado);
    }

    // ---------- evaluarSaturacion: casos límite ----------

    @Test
    void evaluarSaturacion_sinContenedores_noHayAlerta() {
        SaturacionDTO dto = alertaService.evaluarSaturacion();

        assertFalse(dto.isAlertaActiva());
        assertEquals(0, contarAlertasImpresas());
    }

    @Test
    void evaluarSaturacion_todosNaranja_activaAlerta() {
        registrarContenedores(5, 0);

        SaturacionDTO dto = alertaService.evaluarSaturacion();

        assertTrue(dto.isAlertaActiva());
        assertEquals(1, contarAlertasImpresas());
    }

    @Test
    void evaluarSaturacion_ningunoNaranja_noActivaAlerta() {
        registrarContenedores(0, 5);

        SaturacionDTO dto = alertaService.evaluarSaturacion();

        assertFalse(dto.isAlertaActiva());
        assertEquals(0, contarAlertasImpresas());
    }

    @Test
    void evaluarSaturacion_exactamente80Porciento_noActivaAlerta() {
        // 4 de 5 = 80 % -> la condición es estrictamente mayor que 80
        registrarContenedores(4, 1);

        SaturacionDTO dto = alertaService.evaluarSaturacion();

        assertFalse(dto.isAlertaActiva());
        assertEquals(0, contarAlertasImpresas());
    }

    @Test
    void evaluarSaturacion_porEncimaDel80Porciento_activaAlerta() {
        // 5 de 6 = 83,33 %
        registrarContenedores(5, 1);

        SaturacionDTO dto = alertaService.evaluarSaturacion();

        assertTrue(dto.isAlertaActiva());
        assertEquals(1, contarAlertasImpresas());
    }

    // ---------- evaluarSaturacion: lectura del historial ----------

    @Test
    void evaluarSaturacion_soloCuentaElUltimoEstadoDelHistorial() {
        // Estuvo en NARANJA pero el último estado es VERDE -> no cuenta
        alertaService.registrarContenedor(
                crearContenedor(1L, NivelLlenado.NARANJA, NivelLlenado.VERDE));
        // Estuvo en VERDE pero el último estado es NARANJA -> cuenta
        alertaService.registrarContenedor(
                crearContenedor(2L, NivelLlenado.VERDE, NivelLlenado.NARANJA));

        SaturacionDTO dto = alertaService.evaluarSaturacion();

        // 1 de 2 = 50 % -> sin alerta
        assertFalse(dto.isAlertaActiva());
    }

    @Test
    void evaluarSaturacion_historialVacio_noCuentaComoNaranja() {
        alertaService.registrarContenedor(crearContenedor(1L)); // historial vacío
        alertaService.registrarContenedor(crearContenedor(2L, NivelLlenado.NARANJA));

        SaturacionDTO dto = alertaService.evaluarSaturacion();

        // 1 de 2 = 50 % -> sin alerta
        assertFalse(dto.isAlertaActiva());
    }

    @Test
    void evaluarSaturacion_historialNulo_noLanzaExcepcionNiCuenta() {
        alertaService.registrarContenedor(crearContenedorConHistorial(1L, null));

        SaturacionDTO dto = assertDoesNotThrow(() -> alertaService.evaluarSaturacion());

        assertFalse(dto.isAlertaActiva());
    }

    // ---------- evaluarSaturacion: notificaciones ----------

    @Test
    void evaluarSaturacion_alertaYaActiva_noVuelveANotificar() {
        registrarContenedores(5, 0);

        alertaService.evaluarSaturacion();
        alertaService.evaluarSaturacion();
        alertaService.evaluarSaturacion();

        assertEquals(1, contarAlertasImpresas());
    }

    @Test
    void evaluarSaturacion_alertaSeDesactivaYVuelveAActivarse_notificaDosVeces() {
        // Fase 1: todos naranja -> alerta
        alertaService.registrarContenedor(crearContenedor(1L, NivelLlenado.NARANJA));
        assertTrue(alertaService.evaluarSaturacion().isAlertaActiva());

        // Fase 2: el contenedor pasa a VERDE (se sobrescribe por id) -> alerta desactivada
        alertaService.registrarContenedor(crearContenedor(1L, NivelLlenado.VERDE));
        assertFalse(alertaService.evaluarSaturacion().isAlertaActiva());

        // Fase 3: vuelve a NARANJA -> nueva alerta
        alertaService.registrarContenedor(crearContenedor(1L, NivelLlenado.NARANJA));
        assertTrue(alertaService.evaluarSaturacion().isAlertaActiva());

        assertEquals(2, contarAlertasImpresas());
    }

    @Test
    void evaluarSaturacion_mensajeIncluyeElPorcentajeConDosDecimales() {
        // 5 de 6 = 83,33 %
        registrarContenedores(5, 1);

        alertaService.evaluarSaturacion();

        String texto = salida.toString();
        assertTrue(texto.contains("83,33") || texto.contains("83.33"),
                "El mensaje debería incluir el porcentaje con dos decimales. Salida: " + texto);
    }

    // ---------- registrarContenedor ----------

    @Test
    void registrarContenedor_nulo_seIgnora() {
        assertDoesNotThrow(() -> alertaService.registrarContenedor(null));

        assertFalse(alertaService.evaluarSaturacion().isAlertaActiva());
    }

    @Test
    void registrarContenedor_conIdNulo_seIgnora() {
        // Si se hubiera registrado, sería 1 de 1 en NARANJA = 100 % y saltaría la alerta
        alertaService.registrarContenedor(crearContenedor(null, NivelLlenado.NARANJA));

        assertFalse(alertaService.evaluarSaturacion().isAlertaActiva());
    }

    @Test
    void registrarContenedor_mismoId_sobrescribeAlAnterior() {
        alertaService.registrarContenedor(crearContenedor(1L, NivelLlenado.VERDE));
        alertaService.registrarContenedor(crearContenedor(1L, NivelLlenado.NARANJA));

        // Si se hubieran acumulado serían 2 contenedores (50 %); sobrescrito es 1 de 1 (100 %)
        assertTrue(alertaService.evaluarSaturacion().isAlertaActiva());
    }
}