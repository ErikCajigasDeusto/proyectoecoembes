package es.deusto.sd.ecoembes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.dto.CapacidadDTO;
import es.deusto.sd.ecoembes.entity.CapacidadPlanta;
import es.deusto.sd.ecoembes.entity.PlantaReciclaje;

class PlantaServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 3, 15);

    private PlantaService service;

    @BeforeEach
    void setUp() {
        service = new PlantaService(null);
    }

    // ---------- Métodos auxiliares ----------

    private CapacidadPlanta crearCapacidad(LocalDate fecha, double toneladas) {
        CapacidadPlanta capacidad = mock(CapacidadPlanta.class);
        when(capacidad.getFecha()).thenReturn(fecha);
        when(capacidad.getToneladasDisponibles()).thenReturn(toneladas);
        return capacidad;
    }

    private PlantaReciclaje crearPlanta(Long id, CapacidadPlanta... capacidades) {
        PlantaReciclaje planta = mock(PlantaReciclaje.class);
        when(planta.getId()).thenReturn(id);
        when(planta.getCapacidades()).thenReturn(new ArrayList<>(Arrays.asList(capacidades)));
        return planta;
    }

    // ---------- consultarCapacidades ----------

    @Test
    void consultarCapacidades_fechaNula_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.consultarCapacidades(null));

        assertEquals("La fecha no puede ser nula", ex.getMessage());
    }

    @Test
    void consultarCapacidades_sinPlantas_devuelveListaVacia() {
        List<CapacidadPlanta> resultado = service.consultarCapacidades(FECHA);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarCapacidades_plantaSinCapacidades_devuelveListaVacia() {
        service.addPlanta(crearPlanta(1L));

        assertTrue(service.consultarCapacidades(FECHA).isEmpty());
    }

    @Test
    void consultarCapacidades_devuelveSoloLasDeLaFechaIndicada() {
        CapacidadPlanta hoy = crearCapacidad(FECHA, 100.0);
        CapacidadPlanta ayer = crearCapacidad(FECHA.minusDays(1), 50.0);
        CapacidadPlanta manana = crearCapacidad(FECHA.plusDays(1), 70.0);
        service.addPlanta(crearPlanta(1L, hoy, ayer, manana));

        List<CapacidadPlanta> resultado = service.consultarCapacidades(FECHA);

        assertEquals(1, resultado.size());
        assertSame(hoy, resultado.get(0));
    }

    @Test
    void consultarCapacidades_ningunaCoincideConLaFecha_devuelveListaVacia() {
        service.addPlanta(crearPlanta(1L, crearCapacidad(FECHA.minusDays(1), 50.0)));

        assertTrue(service.consultarCapacidades(FECHA).isEmpty());
    }

    @Test
    void consultarCapacidades_capacidadConFechaNula_seIgnora() {
        service.addPlanta(crearPlanta(1L, crearCapacidad(null, 50.0)));

        assertDoesNotThrow(() -> service.consultarCapacidades(FECHA));
        assertTrue(service.consultarCapacidades(FECHA).isEmpty());
    }

    @Test
    void consultarCapacidades_variasPlantas_agrupaLasCapacidadesDeTodas() {
        CapacidadPlanta c1 = crearCapacidad(FECHA, 100.0);
        CapacidadPlanta c2 = crearCapacidad(FECHA, 200.0);
        CapacidadPlanta otraFecha = crearCapacidad(FECHA.plusDays(5), 300.0);
        service.addPlanta(crearPlanta(1L, c1, otraFecha));
        service.addPlanta(crearPlanta(2L, c2));

        List<CapacidadPlanta> resultado = service.consultarCapacidades(FECHA);

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(c1));
        assertTrue(resultado.contains(c2));
    }

    // ---------- addPlanta ----------

    @Test
    void addPlanta_nula_seIgnora() {
        assertDoesNotThrow(() -> service.addPlanta(null));

        assertTrue(service.consultarCapacidades(FECHA).isEmpty());
    }

    @Test
    void addPlanta_mismoId_noSobrescribeLaPrimera() {
        CapacidadPlanta primera = crearCapacidad(FECHA, 100.0);
        CapacidadPlanta segunda = crearCapacidad(FECHA, 999.0);
        service.addPlanta(crearPlanta(1L, primera));
        service.addPlanta(crearPlanta(1L, segunda)); // putIfAbsent: se ignora

        List<CapacidadPlanta> resultado = service.consultarCapacidades(FECHA);

        assertEquals(1, resultado.size());
        assertSame(primera, resultado.get(0));
    }

    // ---------- consultarCapacidadesDTO ----------

    @Test
    void consultarCapacidadesDTO_fechaNula_lanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.consultarCapacidadesDTO(null));

        assertEquals("La fecha no puede ser nula", ex.getMessage());
    }

    @Test
    void consultarCapacidadesDTO_sinPlantas_devuelveListaVacia() {
        List<CapacidadDTO> resultado = service.consultarCapacidadesDTO(FECHA);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarCapacidadesDTO_convierteLosDatosCorrectamente() {
        service.addPlanta(crearPlanta(7L, crearCapacidad(FECHA, 123.5)));

        List<CapacidadDTO> resultado = service.consultarCapacidadesDTO(FECHA);

        assertEquals(1, resultado.size());
        CapacidadDTO dto = resultado.get(0);
        assertEquals(7L, dto.getIdPlanta());
        assertEquals(FECHA, dto.getFecha());
        assertEquals(123.5, dto.getToneladasDisponibles(), 0.001);
    }

    @Test
    void consultarCapacidadesDTO_filtraPorFecha() {
        service.addPlanta(crearPlanta(1L,
                crearCapacidad(FECHA, 100.0),
                crearCapacidad(FECHA.minusDays(1), 50.0)));

        List<CapacidadDTO> resultado = service.consultarCapacidadesDTO(FECHA);

        assertEquals(1, resultado.size());
        assertEquals(100.0, resultado.get(0).getToneladasDisponibles(), 0.001);
    }

    @Test
    void consultarCapacidadesDTO_variasPlantas_incluyeElIdDeCadaPlanta() {
        service.addPlanta(crearPlanta(1L, crearCapacidad(FECHA, 100.0)));
        service.addPlanta(crearPlanta(2L, crearCapacidad(FECHA, 200.0)));

        List<CapacidadDTO> resultado = service.consultarCapacidadesDTO(FECHA);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().anyMatch(d -> d.getIdPlanta().equals(1L)));
        assertTrue(resultado.stream().anyMatch(d -> d.getIdPlanta().equals(2L)));
    }

    @Test
    void consultarCapacidadesDTO_capacidadConFechaNula_seIgnora() {
        service.addPlanta(crearPlanta(1L, crearCapacidad(null, 50.0)));

        assertTrue(service.consultarCapacidadesDTO(FECHA).isEmpty());
    }
}