package es.deusto.sd.ContSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import es.deusto.sd.ContSocket.service.CapacidadService;

/**
 * Servidor-ContSocket: servidor externo de capacidad de la planta (Sockets TCP).
 * Protocolo: DataInputStream.readUTF / DataOutputStream.writeUTF con delimitador '#'.
 */
public class ContSocketServer {

    public static final int DEFAULT_PORT = 9090;
    private static final int DIAS_PRECARGA = 10;
    private static final double CAPACIDAD_INICIAL_TN = 25.0;

    /** Capacidad disponible (toneladas) por fecha. */
    private static final Map<LocalDate, Double> CAPACIDADES = new ConcurrentHashMap<>();
    private static final AtomicInteger NUM_CLIENTES = new AtomicInteger(0);

    static {
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i <= DIAS_PRECARGA; i++) {
            CAPACIDADES.put(hoy.plusDays(i), CAPACIDAD_INICIAL_TN);
        }
    }

    /** Devuelve la capacidad disponible para la fecha, o null si no existe. */
    public static Double getCapacidad(LocalDate fecha) {
        return CAPACIDADES.get(fecha);
    }

    /**
     * Descuenta toneladas de forma atómica.
     * @return true si la fecha existe y había capacidad suficiente.
     */
    public static boolean descontar(LocalDate fecha, double toneladas) {
        AtomicBoolean ok = new AtomicBoolean(false);
        CAPACIDADES.computeIfPresent(fecha, (f, actual) -> {
            if (actual >= toneladas) {
                ok.set(true);
                // Redondeo a 3 decimales para evitar ruido de coma flotante
                return Math.round((actual - toneladas) * 1000.0) / 1000.0;
            }
            return actual;
        });
        return ok.get();
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("# ContSocketServer: puerto invalido '" + args[0]
                        + "', se usa " + DEFAULT_PORT);
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(" - ContSocketServer: escuchando en el puerto " + serverSocket.getLocalPort());
            System.out.println(" - ContSocketServer: capacidades precargadas: "
                    + new java.util.TreeMap<>(CAPACIDADES));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(" - ContSocketServer: cliente #" + NUM_CLIENTES.incrementAndGet()
                        + " conectado desde " + clientSocket.getInetAddress().getHostAddress()
                        + ":" + clientSocket.getPort());
                new Thread(new CapacidadService(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("# ContSocketServer: error de IO: " + e.getMessage());
        }
    }
}