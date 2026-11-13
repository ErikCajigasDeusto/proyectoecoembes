package es.deusto.sd.ecoembes.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.dto.SaturacionDTO;
import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.NivelLlenado;

@Service
public class AlertaService {
	
	// Almacén en memoria del servidor (Prototipo 1)
    private final Map<Long, Contenedor> contenedoresMonitorizados = new ConcurrentHashMap<>();
    private SaturacionDTO estadoActual = new SaturacionDTO(0.0, 0, false);

    public SaturacionDTO consultarSaturacion() {
        return estadoActual;
    }

    public SaturacionDTO evaluarSaturacion() {
        int totalContenedores = contenedoresMonitorizados.size();
        if (totalContenedores == 0) {
            estadoActual = new SaturacionDTO(0.0, 0, false);
            return estadoActual;
        }

        int contenedoresEnNaranja = 0;
        for (Contenedor contenedor : contenedoresMonitorizados.values()) {
            List<EstadoContenedor> historial = contenedor.getEstados();
            // Comprobación de que el historial no sea nulo y tenga al menos un estado
            if (historial != null && !historial.isEmpty()) {
                EstadoContenedor ultimoEstado = historial.get(historial.size() - 1);
                if (ultimoEstado.getNivel() == NivelLlenado.NARANJA) {
                    contenedoresEnNaranja++;
                }
            }
        }

        double porcentajeSaturacion = (contenedoresEnNaranja * 100.0) / totalContenedores;
        boolean alertaActiva = porcentajeSaturacion > 80.0;
        boolean alertaAnterior = estadoActual.isAlertaActiva();

        estadoActual = new SaturacionDTO(porcentajeSaturacion, contenedoresEnNaranja, alertaActiva);

        // Solo notificar cuando la alerta pasa de inactiva a activa
        if (alertaActiva && !alertaAnterior) {
            System.out.println("[ALERTA SATURACIÓN] Se ha superado el 80 % de contenedores en nivel NARANJA ("
                    + String.format("%.2f", porcentajeSaturacion) + "%). Notificando al personal de Ecoembes...");
        }

        return estadoActual;
    }

    public void registrarContenedor(Contenedor contenedor) {
        if (contenedor != null && contenedor.getId() != null) {
            contenedoresMonitorizados.put(contenedor.getId(), contenedor);
        }
    }
}