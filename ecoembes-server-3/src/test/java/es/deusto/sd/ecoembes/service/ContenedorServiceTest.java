package es.deusto.sd.ecoembes.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.dto.ContenedorDTO;
import es.deusto.sd.ecoembes.dto.EstadoDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.NivelLlenado;

class ContenedorServiceTest {
    private ContenedorService service;
    private LocalDateTime fecha;
    private Contenedor contenedor;

    @BeforeEach
    void setUp() {
        service = new ContenedorService(new AlertaService());
        fecha = LocalDateTime.of(2026, 11, 14, 10, 0);
        contenedor = service.crear(new ContenedorDTO(null, "Calle Mayor 10", "48001", 100));
    }

    @Test
    void createsContainerAndGeneratesId() {
        assertNotNull(contenedor.getId());
        assertEquals(1L, contenedor.getId());
        assertEquals("Calle Mayor 10", contenedor.getDireccion());
        assertEquals("48001", contenedor.getZona().getCodigoPostal());
        assertSame(contenedor, service.obtenerPorId(contenedor.getId()));
    }

    @Test
    void rejectsInvalidContainerData() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.crear(new ContenedorDTO(null, "", "48001", 100)));
        assertThrows(IllegalArgumentException.class,
                () -> service.crear(new ContenedorDTO(null, "Calle", "48001", 0)));
    }

    @Test
    void updatesAndConsultsLatestStateForDate() {
        service.actualizarEstado(contenedor.getId(), new EstadoDTO(fecha, 30, NivelLlenado.VERDE));
        service.actualizarEstado(contenedor.getId(), new EstadoDTO(fecha.plusHours(2), 70, NivelLlenado.NARANJA));
        EstadoContenedor estado = service.consultarEstado(contenedor.getId(), fecha.toLocalDate());
        assertNotNull(estado);
        assertEquals(70, estado.getNumeroEnvases());
        assertEquals(NivelLlenado.NARANJA, estado.getNivel());
    }

    @Test
    void rejectsStateForUnknownContainerAndCapacityOverflow() {
        assertThrows(java.util.NoSuchElementException.class,
                () -> service.actualizarEstado(999L, new EstadoDTO(fecha, 10, NivelLlenado.VERDE)));
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarEstado(contenedor.getId(), new EstadoDTO(fecha, 101, NivelLlenado.ROJO)));
        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarEstado(contenedor.getId(), new EstadoDTO(fecha, 10, null)));
    }

    @Test
    void filtersUsageBetweenDates() {
        service.actualizarEstado(contenedor.getId(), new EstadoDTO(fecha.minusDays(2), 10, NivelLlenado.VERDE));
        service.actualizarEstado(contenedor.getId(), new EstadoDTO(fecha, 20, NivelLlenado.NARANJA));
        service.actualizarEstado(contenedor.getId(), new EstadoDTO(fecha.plusDays(2), 30, NivelLlenado.ROJO));
        List<EstadoContenedor> uso = service.consultarUso(contenedor.getId(), fecha.toLocalDate(), fecha.toLocalDate());
        assertEquals(1, uso.size());
        assertEquals(20, uso.get(0).getNumeroEnvases());
    }

    @Test
    void rejectsInvalidUsageDateRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.consultarUso(contenedor.getId(), null, fecha.toLocalDate()));
        assertThrows(IllegalArgumentException.class,
                () -> service.consultarUso(contenedor.getId(), fecha.toLocalDate(), fecha.toLocalDate().minusDays(1)));
    }

    @Test
    void consultsZoneAndReturnsNoStateForUnknownZone() {
        service.actualizarEstado(contenedor.getId(), new EstadoDTO(fecha, 25, NivelLlenado.VERDE));
        assertEquals(1, service.consultarZona("48001", fecha.toLocalDate()).size());
        assertTrue(service.consultarZona("00000", fecha.toLocalDate()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.consultarZona(null, fecha.toLocalDate()));
    }
}
