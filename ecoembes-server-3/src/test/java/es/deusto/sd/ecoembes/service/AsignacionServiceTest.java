package es.deusto.sd.ecoembes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.client.EcoembesClient;
import es.deusto.sd.ecoembes.dto.AsignacionDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.Personal;
import es.deusto.sd.ecoembes.entity.PlantaReciclaje;

class AsignacionServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 3, 15);

    private AsignacionService service;
    private EcoembesClient contSocketClient;

    @BeforeEach
    void setUp() throws IOException {
        // Cliente de sockets simulado: por defecto ContSocket acepta las notificaciones
        contSocketClient = mock(EcoembesClient.class);
        when(contSocketClient.notificar(any(LocalDate.class), anyInt(), anyInt())).thenReturn(true);

        service = new AsignacionService(Optional.of(contSocketClient));

        service.addPlanta(crearPlanta(1L));
        service.addPersonal(crearPersonal(1L));
        service.addContenedor(crearContenedor(1L));
        service.addContenedor(crearContenedor(2L));
        service.addContenedor(crearContenedor(3L));
    }

    // ---------- Métodos auxiliares ----------

    private PlantaReciclaje crearPlanta(Long id) {
        PlantaReciclaje planta = mock(PlantaReciclaje.class);
        when(planta.getId()).thenReturn(id);
        return planta;
    }

    private Personal crearPersonal(Long id) {
        Personal personal = mock(Personal.class);
        when(personal.getId()).thenReturn(id);
        return personal;
    }

    private Contenedor crearContenedor(Long id) {
        Contenedor contenedor = mock(Contenedor.class);
        when(contenedor.getId()).thenReturn(id);
        return contenedor;
    }

    /** Orden del constructor: id, idPlanta, idsContenedores, fecha, totalEnvases, idPersonal. */
    private AsignacionDTO crearDTO(Long idPlanta, Long idPersonal, List<Long> ids, LocalDate fecha) {
        return new AsignacionDTO(null, idPlanta, ids, fecha, 0, idPersonal);
    }

    /** Crea una asignación válida y espera 2 ms: el id se genera con currentTimeMillis(). */
    private AsignacionDTO crearAsignacion(Long idPlanta, LocalDate fecha) throws InterruptedException {
        AsignacionDTO resultado = service.crearAsignacion(crearDTO(idPlanta, 1L, Arrays.asList(1L), fecha));
        Thread.sleep(2);
        return resultado;
    }

    // ---------- crear: caso correcto ----------

    @Test
    void crear_datosValidos_devuelveDTOConLosDatosEsperados() {
        AsignacionDTO dto = crearDTO(1L, 1L, Arrays.asList(1L, 2L), FECHA);

        AsignacionDTO resultado = service.crearAsignacion(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(1L, resultado.getIdPlanta());
        assertEquals(1L, resultado.getIdPersonal());
        assertEquals(FECHA, resultado.getFecha());
        assertEquals(Arrays.asList(1L, 2L), resultado.getIdsContenedores());
    }

    @Test
    void crear_mantieneElOrdenDeLosContenedores() {
        AsignacionDTO dto = crearDTO(1L, 1L, Arrays.asList(3L, 1L, 2L), FECHA);

        AsignacionDTO resultado = service.crearAsignacion(dto);

        assertEquals(Arrays.asList(3L, 1L, 2L), resultado.getIdsContenedores());
    }

    @Test
    void crear_laAsignacionQuedaGuardadaYSePuedeConsultar() {
        AsignacionDTO creada = service.crearAsignacion(crearDTO(1L, 1L, Arrays.asList(1L), FECHA));

        AsignacionDTO consultada = service.consultar(creada.getId());

        assertEquals(creada.getId(), consultada.getId());
        assertEquals(creada.getIdPlanta(), consultada.getIdPlanta());
    }

    // ---------- crear: integración con ContSocket ----------

    @Test
    void crear_notificaAContSocketConFechaYNumeroDeContenedores() throws IOException {
        service.crearAsignacion(crearDTO(1L, 1L, Arrays.asList(1L, 2L), FECHA));

        verify(contSocketClient).notificar(eq(FECHA), eq(2), anyInt());
    }

    @Test
    void crear_contSocketRechazaLaNotificacion_lanzaExcepcionYNoGuarda() throws IOException {
        when(contSocketClient.notificar(any(LocalDate.class), anyInt(), anyInt())).thenReturn(false);
        AsignacionDTO dto = crearDTO(1L, 1L, Arrays.asList(1L), FECHA);

        assertThrows(IllegalArgumentException.class, () -> service.crearAsignacion(dto));

        assertTrue(service.consultarAsignaciones(1L, FECHA.minusYears(1), FECHA.plusYears(1)).isEmpty());
    }

    @Test
    void crear_contSocketNoDisponible_lanzaIllegalStateYNoGuarda() throws IOException {
        when(contSocketClient.notificar(any(LocalDate.class), anyInt(), anyInt()))
                .thenThrow(new IOException("Connection refused"));
        AsignacionDTO dto = crearDTO(1L, 1L, Arrays.asList(1L), FECHA);

        assertThrows(IllegalStateException.class, () -> service.crearAsignacion(dto));

        assertTrue(service.consultarAsignaciones(1L, FECHA.minusYears(1), FECHA.plusYears(1)).isEmpty());
    }

    // ---------- crear: validaciones ----------

    @Test
    void crear_dtoNulo_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearAsignacion(null));

        assertEquals("Datos de asignación incompletos", ex.getMessage());
    }

    @Test
    void crear_sinIdPlanta_lanzaExcepcion() {
        AsignacionDTO dto = crearDTO(null, 1L, Arrays.asList(1L), FECHA);

        assertThrows(IllegalArgumentException.class, () -> service.crearAsignacion(dto));
    }

    @Test
    void crear_sinIdPersonal_lanzaExcepcion() {
        AsignacionDTO dto = crearDTO(1L, null, Arrays.asList(1L), FECHA);

        assertThrows(IllegalArgumentException.class, () -> service.crearAsignacion(dto));
    }

    @Test
    void crear_sinFecha_lanzaExcepcion() {
        AsignacionDTO dto = crearDTO(1L, 1L, Arrays.asList(1L), null);

        assertThrows(IllegalArgumentException.class, () -> service.crearAsignacion(dto));
    }

    @Test
    void crear_listaDeContenedoresNula_lanzaExcepcion() {
        AsignacionDTO dto = crearDTO(1L, 1L, null, FECHA);

        assertThrows(IllegalArgumentException.class, () -> service.crearAsignacion(dto));
    }

    @Test
    void crear_listaDeContenedoresVacia_lanzaExcepcion() {
        AsignacionDTO dto = crearDTO(1L, 1L, Arrays.asList(), FECHA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearAsignacion(dto));

        assertEquals("Datos de asignación incompletos", ex.getMessage());
    }

    @Test
    void crear_plantaInexistente_lanzaExcepcion() {
        AsignacionDTO dto = crearDTO(99L, 1L, Arrays.asList(1L), FECHA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearAsignacion(dto));

        assertEquals("Planta o empleado no encontrado", ex.getMessage());
    }

    @Test
    void crear_empleadoInexistente_lanzaExcepcion() {
        AsignacionDTO dto = crearDTO(1L, 99L, Arrays.asList(1L), FECHA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearAsignacion(dto));

        assertEquals("Planta o empleado no encontrado", ex.getMessage());
    }

    @Test
    void crear_contenedorInexistente_lanzaExcepcionConElId() {
        AsignacionDTO dto = crearDTO(1L, 1L, Arrays.asList(1L, 99L), FECHA);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearAsignacion(dto));

        assertEquals("Contenedor no encontrado: 99", ex.getMessage());
    }

    @Test
    void crear_siFalla_noGuardaNingunaAsignacion() {
        AsignacionDTO dto = crearDTO(1L, 1L, Arrays.asList(1L, 99L), FECHA);

        assertThrows(IllegalArgumentException.class, () -> service.crearAsignacion(dto));

        assertTrue(service.consultarAsignaciones(1L, FECHA.minusYears(1), FECHA.plusYears(1)).isEmpty());
    }

    // ---------- consultarCapacidad ----------

    @Test
    void consultarCapacidad_fechaConRegistro_devuelveToneladas() throws IOException {
        when(contSocketClient.getCapacidad(FECHA)).thenReturn(Optional.of(25.0));

        assertEquals(25.0, service.consultarCapacidad(FECHA));
    }

    @Test
    void consultarCapacidad_fechaSinRegistro_lanzaExcepcion() throws IOException {
        when(contSocketClient.getCapacidad(FECHA)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.consultarCapacidad(FECHA));
    }

    @Test
    void consultarCapacidad_contSocketNoDisponible_lanzaIllegalState() throws IOException {
        when(contSocketClient.getCapacidad(FECHA)).thenThrow(new IOException("Connection refused"));

        assertThrows(IllegalStateException.class, () -> service.consultarCapacidad(FECHA));
    }

    // ---------- consultar ----------

    @Test
    void consultar_idInexistente_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.consultar(12345L));

        assertEquals("Asignación no encontrada", ex.getMessage());
    }

    // ---------- consultarAsignaciones ----------

    @Test
    void consultarAsignaciones_sinAsignaciones_devuelveListaVacia() {
        List<AsignacionDTO> resultado = service.consultarAsignaciones(1L, FECHA.minusDays(1), FECHA.plusDays(1));

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarAsignaciones_dentroDelRango_laIncluye() throws InterruptedException {
        crearAsignacion(1L, FECHA);

        List<AsignacionDTO> resultado = service.consultarAsignaciones(1L, FECHA.minusDays(1), FECHA.plusDays(1));

        assertEquals(1, resultado.size());
    }

    @Test
    void consultarAsignaciones_fechasLimiteSonInclusivas() throws InterruptedException {
        crearAsignacion(1L, FECHA);

        // inicio y fin coinciden con la fecha de la asignación
        List<AsignacionDTO> resultado = service.consultarAsignaciones(1L, FECHA, FECHA);

        assertEquals(1, resultado.size());
    }

    @Test
    void consultarAsignaciones_anteriorAlInicio_laExcluye() throws InterruptedException {
        crearAsignacion(1L, FECHA);

        List<AsignacionDTO> resultado = service.consultarAsignaciones(1L, FECHA.plusDays(1), FECHA.plusDays(10));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarAsignaciones_posteriorAlFin_laExcluye() throws InterruptedException {
        crearAsignacion(1L, FECHA);

        List<AsignacionDTO> resultado = service.consultarAsignaciones(1L, FECHA.minusDays(10), FECHA.minusDays(1));

        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarAsignaciones_filtraPorPlanta() throws InterruptedException {
        service.addPlanta(crearPlanta(2L));
        crearAsignacion(1L, FECHA);
        crearAsignacion(2L, FECHA);

        List<AsignacionDTO> resultado = service.consultarAsignaciones(1L, FECHA.minusDays(1), FECHA.plusDays(1));

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getIdPlanta());
    }

    @Test
    void consultarAsignaciones_variasEnElRango_devuelveTodas() throws InterruptedException {
        crearAsignacion(1L, FECHA);
        crearAsignacion(1L, FECHA.plusDays(3));
        crearAsignacion(1L, FECHA.plusDays(30)); // fuera del rango

        List<AsignacionDTO> resultado = service.consultarAsignaciones(1L, FECHA, FECHA.plusDays(7));

        assertEquals(2, resultado.size());
    }
    
    // ---------- ContSocket desactivado ----------
    private AsignacionService crearServicioSinContSocket() {
        AsignacionService s = new AsignacionService(Optional.empty());
        s.addPlanta(crearPlanta(1L));
        s.addPersonal(crearPersonal(1L));
        s.addContenedor(crearContenedor(1L));
        return s;
    }

    @Test
    void crear_contSocketDesactivado_creaLaAsignacionSinNotificar() {
        AsignacionService sinSocket = crearServicioSinContSocket();

        AsignacionDTO resultado = sinSocket.crearAsignacion(crearDTO(1L, 1L, Arrays.asList(1L), FECHA));

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(FECHA, resultado.getFecha());
    }

    @Test
    void consultarCapacidad_contSocketDesactivado_lanzaIllegalState() {
        AsignacionService sinSocket = crearServicioSinContSocket();

        assertThrows(IllegalStateException.class, () -> sinSocket.consultarCapacidad(FECHA));
    }
}