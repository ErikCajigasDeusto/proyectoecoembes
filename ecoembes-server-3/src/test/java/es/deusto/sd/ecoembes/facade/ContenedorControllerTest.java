
package es.deusto.sd.ecoembes.facade;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.dto.ContenedorDTO;
import es.deusto.sd.ecoembes.dto.EstadoDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.NivelLlenado;
import es.deusto.sd.ecoembes.service.AlertaService;
import es.deusto.sd.ecoembes.service.ContenedorService;

class ContenedorServiceTest {

    private ContenedorService service;
    private LocalDateTime fecha;
    private Contenedor contenedor;

    @BeforeEach
    void setUp() {
        service = new ContenedorService(new AlertaService());

        fecha = LocalDateTime.of(2026, 11, 14, 10, 0);

        contenedor = service.crearContenedor(
                new ContenedorDTO(null, "Calle Mayor 10", "48001", 100));
    }

    @Test
    void createsContainerAndGeneratesId() {

        assertNotNull(contenedor.getId());
        assertEquals(1L, contenedor.getId());

        assertEquals("Calle Mayor 10", contenedor.getDireccion());
        assertEquals("48001", contenedor.getZona().getCodigoPostal());
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
    void rejectsDuplicateContainerId() {

        // El contenedor del setUp ya tiene el ID 1.
        ContenedorDTO dto = new ContenedorDTO(
                1L, "Calle Nueva 20", "48002", 200);

        // Intentar crear otro contenedor con ID 1 debe producir un error.
        assertThrows(IllegalArgumentException.class,
                () -> service.crearContenedor(dto));

        // El contenedor original debe conservar sus datos.
        assertEquals(1L, contenedor.getId());
        assertEquals("Calle Mayor 10", contenedor.getDireccion());
        assertEquals("48001", contenedor.getZona().getCodigoPostal());
    }

    @Test
    void generatesNextIdAfterManualId() {

        // Creamos un contenedor con ID manual 12.
        Contenedor manual = service.crearContenedor(
                new ContenedorDTO(12L, "Calle Manual", "48003", 100));

        assertEquals(12L, manual.getId());

        // El siguiente contenedor automático debe tener ID 13.
        Contenedor automatico = service.crearContenedor(
                new ContenedorDTO(null, "Calle Automatica", "48004", 100));

        assertEquals(13L, automatico.getId());
    }

    @Test
    void updatesAndConsultsLatestStateForDate() {

        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha, 30, NivelLlenado.VERDE));

        service.actualizarEstado(
                contenedor.getId(),
                new EstadoDTO(fecha.plusHours(2), 70, NivelLlenado.NARANJA));

        // Comprobamos que se han guardado los dos estados.
        List<EstadoContenedor> estados = contenedor.getEstados();

        assertEquals(2, estados.size());

        // El último estado debe contener los datos de la segunda actualización.
        EstadoContenedor ultimoEstado = estados.get(estados.size() - 1);

        assertEquals(70, ultimoEstado.getNumeroEnvases());
        assertEquals(NivelLlenado.NARANJA, ultimoEstado.getNivel());
    }

    @Test
    void rejectsStateForUnknownContainerAndCapacityOverflow() {

        assertThrows(java.util.NoSuchElementException.class,
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

        assertThrows(java.util.NoSuchElementException.class,
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
                        null,
                        fecha.toLocalDate()));

        assertThrows(IllegalArgumentException.class,
                () -> service.consultarUso(
                        contenedor.getId(),
                        fecha.toLocalDate(),
                        fecha.toLocalDate().minusDays(1)));
    }
}
