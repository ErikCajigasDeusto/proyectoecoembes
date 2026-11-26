package es.deusto.sd.ContSocket.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import es.deusto.sd.ContSocket.dao.CapacidadDAO;
import es.deusto.sd.ContSocket.dao.CapacidadDAOJpa;
import es.deusto.sd.ContSocket.entity.Capacidad;
import es.deusto.sd.ContSocket.service.CapacidadService;

/**
 * Servidor-ContSocket: servidor externo de capacidad de la planta (Sockets TCP).
 * Protocolo: DataInputStream.readUTF / DataOutputStream.writeUTF con delimitador '#'.
 * La persistencia se delega en un CapacidadDAO.
 */
public class ContSocketServer {

    public static final int DEFAULT_PORT = 8083;
    private static final int DIAS_PRECARGA = 10;
    private static final double CAPACIDAD_INICIAL_TN = 25.0;

    private static final AtomicInteger NUM_CLIENTES = new AtomicInteger(0);

    /** DAO compartido por todos los hilos (el EntityManagerFactory es thread-safe). */
    private static final CapacidadDAO DAO = new CapacidadDAOJpa();

    /** Devuelve la capacidad disponible para la fecha, o null si no existe. */
    public static Double getCapacidad(LocalDate fecha) {
        return DAO.findByFecha(fecha)
                .map(Capacidad::getCapacidadDisponible)
                .orElse(null);
    }

    /**
     * Descuenta toneladas de forma atómica.
     * @return true si la fecha existe y había capacidad suficiente.
     */
    public static boolean descontar(LocalDate fecha, double toneladas) {
        return DAO.descontar(fecha, toneladas);
    }

    /** Solo crea las fechas que aún no existen, así se conserva lo persistido. */
    private static void precargar() {
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i <= DIAS_PRECARGA; i++) {
            LocalDate fecha = hoy.plusDays(i);
            if (DAO.findByFecha(fecha).isEmpty()) {
                DAO.save(new Capacidad(CapacidadDAOJpa.PLANTA, fecha,
                        CAPACIDAD_INICIAL_TN, CAPACIDAD_INICIAL_TN));
            }
        }
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

        precargar();
        Runtime.getRuntime().addShutdownHook(new Thread(DAO::close));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(" - ContSocketServer: escuchando en el puerto " + serverSocket.getLocalPort());
            System.out.println(" - ContSocketServer: capacidad de hoy: " + getCapacidad(LocalDate.now()));

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