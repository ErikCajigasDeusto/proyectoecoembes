package es.deusto.sd.ecoembes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.dto.ContenedorDTO;
import es.deusto.sd.ecoembes.dto.EstadoDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.NivelLlenado;

class ContenedorServiceTest {

    private AlertaService alertaService;
    private ContenedorService service;
    private LocalDateTime fecha;
    private Contenedor contenedor;

    @BeforeEach
    void setUp() {
        alertaService = spy(new AlertaService());
        service = new ContenedorService(alertaService);

        fecha = LocalDateTime.of(2026, 11, 14, 10, 0);

        contenedor = service.crearContenedor(
                new ContenedorDTO(null, "Calle Mayor 10", "48001", 100));
    }

    @Test
    void createsContainerAndGeneratesSequentialId() {
        assertNotNull(contenedor.getId());
        assertEquals(1L, contenedor.getId());
        assertEquals("Calle Mayor 10", contenedor.getDireccion());
        assertEquals("48001", contenedor.getZona().getCodigoPostal());

        // Verifica que al crear se registra automáticamente en AlertaService
        verify(alertaService).registrarContenedor(contenedor);
    }

    @Test
    void ignoresManualIdInDtoAndGeneratesSequentialId() {
        // Aunque el DTO traiga un ID manual (12L), el servicio genera el siguiente secuencial (2L)
        Contenedor segundo = service.crearContenedor(
                new ContenedorDTO(12L, "Calle Nueva 20", "48002", 200));

        assertEquals(2L, segundo.getId());

        Contenedor tercero = service.crearContenedor(
                new ContenedorDTO(1L, "Calle Tercera 30", "48003", 150));

        assertEquals(3L, tercero.getId());
        // El contenedor original con ID 1 sigue intacto
        assertEquals("Calle Mayor 10", service.obtenerPorId(1L).getDireccion());
    }

    @Test
    void rejectsInvalidContainerData() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearContenedor(null));

        assertThrows(IllegalArgumentException.class,
                () -> service.crearContenedor(
                        new ContenedorDTO(null, "", "48001", 100)));

        assertThrows(IllegalArgumentException.class,
                () -> service.crearContenedor(
                        new ContenedorDTO(null, "Calle", "48001", 0)));

        assertThrows(IllegalArgumentException.class,
                () -> service.crearContenedor(
                        new ContenedorDTO(null, "Calle", "", 100)));
    }

    @Test
    void updatesStateAndTriggersSaturationEvaluation() {
        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha, 30, NivelLlenado.VERDE));

        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha.plusHours(2), 70, NivelLlenado.NARANJA));

        List<EstadoContenedor> estados = contenedor.getEstados();
        assertEquals(2, estados.size());

        EstadoContenedor ultimoEstado = estados.get(estados.size() - 1);
        assertEquals(70, ultimoEstado.getNumeroEnvases());
        assertEquals(NivelLlenado.NARANJA, ultimoEstado.getNivel());

        // Verifica que cada actualización dispara la evaluación interna de alertas
        verify(alertaService, org.mockito.Mockito.times(2)).evaluarSaturacion();
    }

    @Test
    void rejectsStateForUnknownContainerAndCapacityOverflow() {
        assertThrows(NoSuchElementException.class,
                () -> service.actualizarEstado(
                        999L,
                        new EstadoDTO(fecha, 10, NivelLlenado.VERDE)));

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarEstado(
                        contenedor.getId(),
                        new EstadoDTO(fecha, 101, NivelLlenado.ROJO)));

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarEstado(
                        contenedor.getId(),
                        new EstadoDTO(fecha, 10, null)));
    }

    @Test
    void consultsLatestStateForDateAndZone() {
        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha, 30, NivelLlenado.VERDE));
        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha.plusHours(3), 85, NivelLlenado.ROJO));

        // Debe devolver la lectura más reciente de ese día (85 envases, ROJO)
        EstadoContenedor estadoDia = service.consultarEstado(
                contenedor.getId(), fecha.toLocalDate());
        assertNotNull(estadoDia);
        assertEquals(85, estadoDia.getNumeroEnvases());
        assertEquals(NivelLlenado.ROJO, estadoDia.getNivel());

        // Consulta por código postal "48001"
        List<EstadoContenedor> estadosZona = service.consultarZona(
                "48001", fecha.toLocalDate());
        assertEquals(1, estadosZona.size());
        assertEquals(NivelLlenado.ROJO, estadosZona.get(0).getNivel());
    }

    @Test
    void filtersUsageBetweenDates() {
        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha.minusDays(2), 10, NivelLlenado.VERDE));

        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha, 20, NivelLlenado.NARANJA));

        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha.plusDays(2), 30, NivelLlenado.ROJO));

        List<EstadoContenedor> uso = service.consultarUso(
                contenedor.getId(),
                fecha.toLocalDate(),
                fecha.toLocalDate());

        assertEquals(1, uso.size());
        assertEquals(20, uso.get(0).getNumeroEnvases());
        assertEquals(NivelLlenado.NARANJA, uso.get(0).getNivel());
    }

    @Test
    void rejectsUsageForUnknownContainer() {
        assertThrows(NoSuchElementException.class,
                () -> service.consultarUso(
                        999L,
                        fecha.toLocalDate(),
                        fecha.toLocalDate()));
    }

    @Test
    void rejectsInvalidUsageDateRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consultarUso(
                        contenedor.getId(),
                        fecha.toLocalDate(),
                        fecha.toLocalDate().minusDays(1)));
    }
}