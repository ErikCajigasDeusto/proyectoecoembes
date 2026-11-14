/**
 * Initially generated with ChatGPT and adapted to the Ecoembes project.
 *
 * Initializes in-memory test data for employees, recycling plants,
 * containers, container states and assignments.
 */

package es.deusto.sd.ecoembes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.deusto.sd.ecoembes.dto.AsignacionDTO;
import es.deusto.sd.ecoembes.dto.ContenedorDTO;
import es.deusto.sd.ecoembes.dto.EstadoDTO;
import es.deusto.sd.ecoembes.entity.CapacidadPlanta;
import es.deusto.sd.ecoembes.entity.NivelLlenado;
import es.deusto.sd.ecoembes.entity.Personal;
import es.deusto.sd.ecoembes.entity.PlantaReciclaje;
import es.deusto.sd.ecoembes.service.AsignacionService;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.ContenedorService;
import es.deusto.sd.ecoembes.service.PlantaService;

@Configuration
public class DataInitializer {

    private static final Logger logger =
            LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(
            AuthService authService,
            ContenedorService contenedorService,
            PlantaService plantaService,
            AsignacionService asignacionService) {

        return args -> {

            LocalDate hoy = LocalDate.now();
            LocalDateTime ahora = LocalDateTime.now();

            // --------------------------------------------------
            // 1. PERSONAL
            // --------------------------------------------------

            Personal ana = new Personal(
                    1L,
                    "Ana García",
                    "ana@ecoembes.com",
                    "Ana1234!"
            );

            Personal iker = new Personal(
                    2L,
                    "Iker López",
                    "iker@ecoembes.com",
                    "Iker1234!"
            );

            Personal marta = new Personal(
                    3L,
                    "Marta Ruiz",
                    "marta@ecoembes.com",
                    "Marta1234!"
            );

            authService.addPersonal(ana);
            authService.addPersonal(iker);
            authService.addPersonal(marta);

            logger.info("Personal de prueba inicializado.");

            // --------------------------------------------------
            // 2. PLANTAS DE RECICLAJE Y CAPACIDADES
            // --------------------------------------------------

            PlantaReciclaje plantaBilbao = new PlantaReciclaje(
                    1L,
                    "Polígono Industrial Asua, Bilbao",
                    "48950",
                    "Planta de Reciclaje de Bilbao"
            );

            PlantaReciclaje plantaVitoria = new PlantaReciclaje(
                    2L,
                    "Polígono Industrial Jundiz, Vitoria-Gasteiz",
                    "01015",
                    "Planta de Reciclaje de Vitoria"
            );

            // Capacidades de la planta de Bilbao
            plantaBilbao.getCapacidades().add(
                    new CapacidadPlanta(hoy, 125.0)
            );

            plantaBilbao.getCapacidades().add(
                    new CapacidadPlanta(hoy.plusDays(1), 110.0)
            );

            // Capacidades de la planta de Vitoria
            plantaVitoria.getCapacidades().add(
                    new CapacidadPlanta(hoy, 90.0)
            );

            plantaVitoria.getCapacidades().add(
                    new CapacidadPlanta(hoy.plusDays(1), 85.0)
            );

            plantaService.addPlanta(plantaBilbao);
            plantaService.addPlanta(plantaVitoria);

            logger.info("Plantas y capacidades inicializadas.");

            // Registramos las plantas y el personal
            // en el servicio de asignaciones.

            asignacionService.addPlanta(plantaBilbao);
            asignacionService.addPlanta(plantaVitoria);

            asignacionService.addPersonal(ana);
            asignacionService.addPersonal(iker);
            asignacionService.addPersonal(marta);

            // --------------------------------------------------
            // 3. CONTENEDORES
            // --------------------------------------------------

            var contenedor1 = contenedorService.crear(
                    new ContenedorDTO(
                            null,
                            "Calle Gran Vía 10",
                            "48001",
                            100
                    )
            );

            var contenedor2 = contenedorService.crear(
                    new ContenedorDTO(
                            null,
                            "Calle Diputación 15",
                            "48001",
                            120
                    )
            );

            var contenedor3 = contenedorService.crear(
                    new ContenedorDTO(
                            null,
                            "Calle Ercilla 20",
                            "48009",
                            150
                    )
            );

            var contenedor4 = contenedorService.crear(
                    new ContenedorDTO(
                            null,
                            "Calle Dato 8",
                            "01001",
                            100
                    )
            );

            var contenedor5 = contenedorService.crear(
                    new ContenedorDTO(
                            null,
                            "Avenida Gasteiz 25",
                            "01005",
                            130
                    )
            );

            logger.info("Contenedores inicializados.");

            // Registramos los contenedores en el servicio
            // de asignaciones.

            asignacionService.addContenedor(contenedor1);
            asignacionService.addContenedor(contenedor2);
            asignacionService.addContenedor(contenedor3);
            asignacionService.addContenedor(contenedor4);
            asignacionService.addContenedor(contenedor5);

            // --------------------------------------------------
            // 4. ESTADOS DE LOS CONTENEDORES
            // --------------------------------------------------

            // Contenedor 1
            contenedorService.actualizarEstado(
                    contenedor1.getId(),
                    new EstadoDTO(
                            ahora.minusDays(2),
                            35,
                            NivelLlenado.VERDE
                    )
            );

            contenedorService.actualizarEstado(
                    contenedor1.getId(),
                    new EstadoDTO(
                            ahora,
                            55,
                            NivelLlenado.NARANJA
                    )
            );

            // Contenedor 2
            contenedorService.actualizarEstado(
                    contenedor2.getId(),
                    new EstadoDTO(
                            ahora.minusDays(1),
                            40,
                            NivelLlenado.VERDE
                    )
            );

            contenedorService.actualizarEstado(
                    contenedor2.getId(),
                    new EstadoDTO(
                            ahora,
                            95,
                            NivelLlenado.ROJO
                    )
            );

            // Contenedor 3
            contenedorService.actualizarEstado(
                    contenedor3.getId(),
                    new EstadoDTO(
                            ahora,
                            70,
                            NivelLlenado.NARANJA
                    )
            );

            // Contenedor 4
            contenedorService.actualizarEstado(
                    contenedor4.getId(),
                    new EstadoDTO(
                            ahora,
                            20,
                            NivelLlenado.VERDE
                    )
            );

            // Contenedor 5
            contenedorService.actualizarEstado(
                    contenedor5.getId(),
                    new EstadoDTO(
                            ahora,
                            115,
                            NivelLlenado.NARANJA
                    )
            );

            logger.info("Estados de contenedores inicializados.");

            // --------------------------------------------------
            // 5. ASIGNACIONES
            // --------------------------------------------------

            // Asignación de contenedores a la planta de Bilbao

            AsignacionDTO asignacionBilbao = new AsignacionDTO(
                    null,
                    plantaBilbao.getId(),
                    Arrays.asList(
                            contenedor1.getId(),
                            contenedor2.getId(),
                            contenedor3.getId()
                    ),
                    hoy,
                    0,
                    ana.getId()
            );

            asignacionService.crear(asignacionBilbao);

            // Asignación de contenedores a la planta de Vitoria

            AsignacionDTO asignacionVitoria = new AsignacionDTO(
                    null,
                    plantaVitoria.getId(),
                    Arrays.asList(
                            contenedor4.getId(),
                            contenedor5.getId()
                    ),
                    hoy,
                    0,
                    iker.getId()
            );

            asignacionService.crear(asignacionVitoria);

            logger.info("Asignaciones inicializadas.");

            // --------------------------------------------------
            // FIN DE LA INICIALIZACIÓN
            // --------------------------------------------------

            logger.info("Datos de prueba de Ecoembes cargados correctamente.");
        };
    }
}