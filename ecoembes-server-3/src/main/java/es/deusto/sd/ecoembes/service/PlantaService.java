
package es.deusto.sd.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.PlantaReciclaje;
import es.deusto.sd.ecoembes.entity.CapacidadPlanta;

@Service
public class PlantaService {

    // Simulamos las plantas en memoria, como en AuctionsService
    private final Map<Long, PlantaReciclaje> plantas =
            new ConcurrentHashMap<>();

    // Consultar las capacidades de las plantas para una fecha
    public List<CapacidadPlanta> consultarCapacidades(LocalDate fecha) {

        List<CapacidadPlanta> resultado = new ArrayList<>();

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha no puede ser nula"
            );
        }

        // Recorrer todas las plantas registradas
        for (PlantaReciclaje planta : plantas.values()) {

            // Buscar las capacidades de esa planta
            for (CapacidadPlanta capacidad : planta.getCapacidades()) {

                // Comprobar que corresponda a la fecha solicitada
                if (fecha.equals(capacidad.getFecha())) {
                    resultado.add(capacidad);
                }
            }
        }

        return resultado;
    }

    // Registrar una planta de prueba
    public void addPlanta(PlantaReciclaje planta) {

        if (planta != null) {
            plantas.putIfAbsent(planta.getId(), planta);
        }
    }
}
