package es.deusto.sd.ecoembes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.entity.Camion;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.NivelLlenado;
import es.deusto.sd.ecoembes.entity.Ruta;
import es.deusto.sd.ecoembes.entity.Zona;

class RutaServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 3, 15, 10, 0);

    private RutaService service;

    @BeforeEach
    void setUp() {
        service = new RutaService();
    }

    // ---------- Métodos auxiliares ----------

    private Camion crearCamion(Long id) {
        Camion camion = mock(Camion.class);
        when(camion.getId()).thenReturn(id);
        // Lista real para poder comprobar que el servicio añade la ruta
        when(camion.getRutas()).thenReturn(new ArrayList<>());
        return camion;
    }

    private EstadoContenedor crearEstado(NivelLlenado nivel, LocalDateTime fecha) {
        EstadoContenedor estado = mock(EstadoContenedor.class);
        when(estado.getNivel()).thenReturn(nivel);
        when(estado.getFecha()).thenReturn(fecha);
        return estado;
    }

    /** Contenedor con zona (código postal) y los estados indicados. Si codigoPostal es null, la zona es null. */
    private Contenedor crearContenedor(Long id, String codigoPostal, EstadoContenedor... estados) {
        Contenedor contenedor = mock(Contenedor.class);
        when(contenedor.getId()).thenReturn(id);
        when(contenedor.getEstados()).thenReturn(new ArrayList<>(List.of(estados)));
        if (codigoPostal != null) {
            Zona zona = mock(Zona.class);
            when(zona.getCodigoPostal()).thenReturn(codigoPostal);
            when(contenedor.getZona()).thenReturn(zona);
        } else {
            when(contenedor.getZona()).thenReturn(null);
        }
        return contenedor;
    }

    /** Contenedor con un único estado del nivel indicado. */
    private Contenedor contenedorConNivel(Long id, String codigoPostal, NivelLlenado nivel) {
        return crearContenedor(id, codigoPostal, crearEstado(nivel, AHORA));
    }

    private List<Long> idsDe(Ruta ruta) {
        return ruta.getContenedores().stream().map(Contenedor::getId).collect(Collectors.toList());
    }

    // ---------- Casos sin ruta ----------

    @Test
    void generarRutas_sinContenedoresNiCamiones_devuelveListaVacia() {
        List<Ruta> resultado = service.generarRutasOptimas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void generarRutas_conContenedoresPeroSinCamiones_devuelveListaVacia() {
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.ROJO));

        assertTrue(service.generarRutasOptimas().isEmpty());
    }

    @Test
    void generarRutas_conCamionesPeroSinContenedores_devuelveListaVacia() {
        service.addCamion(crearCamion(1L));

        assertTrue(service.generarRutasOptimas().isEmpty());
    }

    @Test
    void generarRutas_soloContenedoresVerdes_devuelveListaVacia() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.VERDE));

        assertTrue(service.generarRutasOptimas().isEmpty());
    }

    // ---------- Selección de contenedores ----------

    @Test
    void generarRutas_contenedorNaranja_seSeleccionaYGeneraUnaRuta() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.NARANJA));

        List<Ruta> resultado = service.generarRutasOptimas();

        assertEquals(1, resultado.size());
        assertEquals(List.of(1L), idsDe(resultado.get(0)));
    }

    @Test
    void generarRutas_contenedorRojo_seSeleccionaYGeneraUnaRuta() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.ROJO));

        List<Ruta> resultado = service.generarRutasOptimas();

        assertEquals(1, resultado.size());
        assertEquals(List.of(1L), idsDe(resultado.get(0)));
    }

    @Test
    void generarRutas_mezclaDeNiveles_soloIncluyeNaranjaYRojo() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.NARANJA));
        service.addContenedor(contenedorConNivel(2L, "48002", NivelLlenado.VERDE));
        service.addContenedor(contenedorConNivel(3L, "48003", NivelLlenado.ROJO));

        Ruta ruta = service.generarRutasOptimas().get(0);

        assertEquals(2, ruta.getContenedores().size());
        assertTrue(idsDe(ruta).contains(1L));
        assertTrue(idsDe(ruta).contains(3L));
        assertFalse(idsDe(ruta).contains(2L));
    }

    @Test
    void generarRutas_contenedorSinEstados_noSeSelecciona() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(crearContenedor(1L, "48001")); // sin estados

        assertTrue(service.generarRutasOptimas().isEmpty());
    }

    @Test
    void generarRutas_estadosConFechaNula_seIgnoran() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(crearContenedor(1L, "48001", crearEstado(NivelLlenado.ROJO, null)));

        assertTrue(service.generarRutasOptimas().isEmpty());
    }

    @Test
    void generarRutas_usaElEstadoConFechaMasReciente_noElUltimoDeLaLista() {
        service.addCamion(crearCamion(1L));
        // El más reciente (VERDE) aparece PRIMERO en la lista; el antiguo (ROJO) al final
        EstadoContenedor reciente = crearEstado(NivelLlenado.VERDE, AHORA);
        EstadoContenedor antiguo = crearEstado(NivelLlenado.ROJO, AHORA.minusDays(2));
        service.addContenedor(crearContenedor(1L, "48001", reciente, antiguo));

        assertTrue(service.generarRutasOptimas().isEmpty());
    }

    @Test
    void generarRutas_elEstadoMasRecienteEsNaranja_seSelecciona() {
        service.addCamion(crearCamion(1L));
        EstadoContenedor antiguo = crearEstado(NivelLlenado.VERDE, AHORA.minusDays(2));
        EstadoContenedor reciente = crearEstado(NivelLlenado.NARANJA, AHORA);
        service.addContenedor(crearContenedor(1L, "48001", antiguo, reciente));

        assertEquals(1, service.generarRutasOptimas().size());
    }

    // ---------- Ordenación ----------

    @Test
    void generarRutas_ordenaContenedoresPorCodigoPostal() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "48003", NivelLlenado.ROJO));
        service.addContenedor(contenedorConNivel(2L, "01001", NivelLlenado.ROJO));
        service.addContenedor(contenedorConNivel(3L, "20001", NivelLlenado.ROJO));

        Ruta ruta = service.generarRutasOptimas().get(0);

        assertEquals(List.of(2L, 3L, 1L), idsDe(ruta));
    }

    @Test
    void generarRutas_contenedorSinZona_seOrdenaAlPrincipio() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "01001", NivelLlenado.ROJO));
        service.addContenedor(contenedorConNivel(2L, null, NivelLlenado.ROJO)); // zona null -> ""

        Ruta ruta = service.generarRutasOptimas().get(0);

        assertEquals(List.of(2L, 1L), idsDe(ruta));
    }

    // ---------- Datos de la ruta ----------

    @Test
    void generarRutas_calculaDistanciaYDuracionSimuladas() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.ROJO));
        service.addContenedor(contenedorConNivel(2L, "48002", NivelLlenado.ROJO));
        service.addContenedor(contenedorConNivel(3L, "48003", NivelLlenado.NARANJA));

        Ruta ruta = service.generarRutasOptimas().get(0);

        // 3 contenedores -> 2 km y 10 min por contenedor
        assertEquals(6.0, ruta.getDistanciaKm(), 0.001);
        assertEquals(30.0, ruta.getDuracionEstimada(), 0.001);
    }

    @Test
    void generarRutas_asignaLaRutaAlCamion() {
        Camion camion = crearCamion(1L);
        service.addCamion(camion);
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.ROJO));

        Ruta ruta = service.generarRutasOptimas().get(0);

        assertEquals(1, camion.getRutas().size());
        assertSame(ruta, camion.getRutas().get(0));
    }

    @Test
    void generarRutas_variosCamiones_usaUnoDeLosRegistrados() {
        Camion camion1 = crearCamion(1L);
        Camion camion2 = crearCamion(2L);
        service.addCamion(camion1);
        service.addCamion(camion2);
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.ROJO));

        service.generarRutasOptimas();

        // La ruta solo se añade a un camión
        assertEquals(1, camion1.getRutas().size() + camion2.getRutas().size());
    }

    @Test
    void generarRutas_llamadasSucesivas_generanIdsDistintos() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.ROJO));

        Ruta primera = service.generarRutasOptimas().get(0);
        Ruta segunda = service.generarRutasOptimas().get(0); // puede ser en el mismo milisegundo

        assertNotEquals(primera.getId(), segunda.getId());
    }

    // ---------- addCamion / addContenedor ----------

    @Test
    void addCamion_nulo_seIgnora() {
        assertDoesNotThrow(() -> service.addCamion(null));

        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.ROJO));
        assertTrue(service.generarRutasOptimas().isEmpty()); // sigue sin haber camiones
    }

    @Test
    void addContenedor_nulo_seIgnora() {
        assertDoesNotThrow(() -> service.addContenedor(null));

        service.addCamion(crearCamion(1L));
        assertTrue(service.generarRutasOptimas().isEmpty()); // sigue sin haber contenedores
    }

    @Test
    void addContenedor_mismoId_sobrescribeAlAnterior() {
        service.addCamion(crearCamion(1L));
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.ROJO));
        service.addContenedor(contenedorConNivel(1L, "48001", NivelLlenado.VERDE));

        assertTrue(service.generarRutasOptimas().isEmpty());
    }
}