package es.deusto.sd.ecoembes.service;

import java.net.Socket;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.Contenedor;
import es.deusto.sd.ecoembes.entity.EstadoContenedor;
import es.deusto.sd.ecoembes.entity.NivelLlenado;

@Service
public class AlertaService implements Runnable {

    // Almacén en memoria del servidor (Prototipo 1)
    private final Map<Long, Contenedor> contenedoresMonitorizados = new ConcurrentHashMap<>();
    private boolean alertaActiva = false;

    public AlertaService(Socket socket) {
		// TODO Auto-generated constructor stub
	}

	public void evaluarSaturacion() {
        int totalContenedores = contenedoresMonitorizados.size();
        if (totalContenedores == 0) {
            alertaActiva = false;
            return;
        }

        int contenedoresSaturados = 0;
        for (Contenedor contenedor : contenedoresMonitorizados.values()) {
            List<EstadoContenedor> historial = contenedor.getEstados();
            if (historial != null && !historial.isEmpty()) {
                EstadoContenedor ultimoEstado = historial.stream()
                        .max(Comparator.comparing(EstadoContenedor::getFecha))
                        .orElse(null);

                if (ultimoEstado != null && (ultimoEstado.getNivel() == NivelLlenado.NARANJA
                        || ultimoEstado.getNivel() == NivelLlenado.ROJO)) {
                    contenedoresSaturados++;
                }
            }
        }

        double porcentajeSaturacion = (contenedoresSaturados * 100.0) / totalContenedores;
        boolean nuevaAlertaActiva = porcentajeSaturacion > 80.0;

        // Solo notificar cuando la alerta pasa de inactiva a activa
        if (nuevaAlertaActiva && !alertaActiva) {
            System.out.println("[ALERTA SATURACIÓN] Se ha superado el 80 % de contenedores en nivel alto (NARANJA/ROJO) ("
                    + String.format("%.2f", porcentajeSaturacion) + "%). Notificando por correo al personal de Ecoembes...");
        }

        alertaActiva = nuevaAlertaActiva;
    }

    public void registrarContenedor(Contenedor contenedor) {
        if (contenedor != null && contenedor.getId() != null) {
            contenedoresMonitorizados.put(contenedor.getId(), contenedor);
        }
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}