
package es.deusto.sd.ecoembes.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.Camion;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.NivelLlenado;
import es.deusto.sd.ecoembes.entity.Ruta;

@Service
public class RutaService {

    // Simulamos los datos en memoria, como en AuctionsService
    private final Map<Long, Ruta> rutas =
            new ConcurrentHashMap<>();

    private final Map<Long, Camion> camiones =
            new ConcurrentHashMap<>();

    private final Map<Long, Contenedor> contenedores =
            new ConcurrentHashMap<>();

    // Generar rutas de recogida simuladas
    public List<Ruta> generarRutasOptimas() {

        List<Ruta> resultado = new ArrayList<>();
        List<Contenedor> seleccionados = new ArrayList<>();

        // Buscar los contenedores que necesitan recogida
        for (Contenedor contenedor : contenedores.values()) {

            EstadoContenedor ultimoEstado = null;

            for (EstadoContenedor estado : contenedor.getEstados()) {

                if (estado.getFecha() != null &&
                        (ultimoEstado == null ||
                         estado.getFecha().isAfter(
                                 ultimoEstado.getFecha()))) {

                    ultimoEstado = estado;
                }
            }

            // Seleccionar niveles ROJO y NARANJA
            if (ultimoEstado != null &&
                    (ultimoEstado.getNivel() == NivelLlenado.ROJO ||
                     ultimoEstado.getNivel() == NivelLlenado.NARANJA)) {

                seleccionados.add(contenedor);
            }
        }

        // Si no hay contenedores o camiones, devolver lista vacía
        if (seleccionados.isEmpty() || camiones.isEmpty()) {
            return resultado;
        }

        // Ordenar primero por código postal
        seleccionados.sort(
                Comparator.comparing(
                        c -> c.getZona() == null
                                ? ""
                                : c.getZona().getCodigoPostal()
                )
        );

        // Elegir un camión de los disponibles
        Camion camion = camiones.values().iterator().next();

        // Generar identificador de la ruta
        long id = System.currentTimeMillis();

        while (rutas.containsKey(id)) {
            id++;
        }

        // Simular distancia y duración
        double distanciaKm = seleccionados.size() * 2.0;
        double duracionEstimada = seleccionados.size() * 10.0;

        // Crear la ruta usando el constructor de Ruta.java
        Ruta ruta = new Ruta(
                id,
                LocalDateTime.now(),
                null,
                duracionEstimada,
                distanciaKm,
                camion
        );

        // Añadir los contenedores a la ruta
        ruta.setContenedores(seleccionados);

        // Mantener la relación con Camion
        camion.getRutas().add(ruta);

        // Guardar la ruta en memoria
        rutas.put(ruta.getId(), ruta);

        resultado.add(ruta);

        return resultado;
    }

    // Añadir camiones de prueba
    public void addCamion(Camion camion) {
        if (camion != null) {
            camiones.put(camion.getId(), camion);
        }
    }

    // Añadir contenedores de prueba
    public void addContenedor(Contenedor contenedor) {
        if (contenedor != null) {
            contenedores.put(contenedor.getId(), contenedor);
        }
    }
}
