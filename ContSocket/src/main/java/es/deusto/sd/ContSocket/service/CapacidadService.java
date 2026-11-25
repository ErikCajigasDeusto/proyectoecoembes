package es.deusto.sd.ContSocket.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import es.deusto.sd.ContSocket.ContSocketServer;

/**
 * Atiende a un cliente. Comandos soportados:
 *   GET_CAPACIDAD#YYYY-MM-DD
 *   NOTIFICAR#YYYY-MM-DD#numContenedores#totalEnvases
 */
public class CapacidadService implements Runnable {

    private static final String DELIMITER = "#";
    /** Toneladas por envase. */
    private static final double TN_POR_ENVASE = 0.02;

    private static final String ERR_FECHA = "ERROR#Fecha no disponible";
    private static final String ERR_CAPACIDAD = "ERROR#Capacidad insuficiente o fecha invalida";
    private static final String ERR_COMANDO = "ERROR#Comando desconocido";

    private final Socket socket;

    public CapacidadService(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String cliente = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        try (Socket s = socket;
             DataInputStream in = new DataInputStream(s.getInputStream());
             DataOutputStream out = new DataOutputStream(s.getOutputStream())) {

            while (true) {
                String peticion;
                try {
                    peticion = in.readUTF();
                } catch (EOFException e) {
                    break; // el cliente cerró la conexión
                }
                System.out.println("   [" + cliente + "] Peticion recibida: " + peticion);
                String respuesta = procesar(peticion);
                out.writeUTF(respuesta);
                out.flush();
                System.out.println("   [" + cliente + "] Respuesta enviada:  " + respuesta);
            }
        } catch (IOException e) {
            System.err.println("# ContSocketService [" + cliente + "]: error de IO: " + e.getMessage());
        }
        System.out.println(" - ContSocketService [" + cliente + "]: conexion cerrada");
    }

    private String procesar(String peticion) {
        String[] partes = peticion.trim().split(DELIMITER, -1);
        switch (partes[0].toUpperCase()) {
            case "GET_CAPACIDAD":
                return getCapacidad(partes);
            case "NOTIFICAR":
                return notificar(partes);
            default:
                return ERR_COMANDO;
        }
    }

    private String getCapacidad(String[] partes) {
        if (partes.length != 2) {
            return ERR_FECHA;
        }
        try {
            Double capacidad = ContSocketServer.getCapacidad(LocalDate.parse(partes[1]));
            return capacidad == null ? ERR_FECHA : "OK#" + capacidad;
        } catch (DateTimeParseException e) {
            return ERR_FECHA;
        }
    }

    private String notificar(String[] partes) {
        if (partes.length != 4) {
            return ERR_CAPACIDAD;
        }
        try {
            LocalDate fecha = LocalDate.parse(partes[1]);
            int numContenedores = Integer.parseInt(partes[2].trim());
            int totalEnvases = Integer.parseInt(partes[3].trim());
            if (numContenedores < 0 || totalEnvases < 0) {
                return ERR_CAPACIDAD;
            }
            double toneladas = totalEnvases * TN_POR_ENVASE;
            if (ContSocketServer.descontar(fecha, toneladas)) {
                System.out.println("   Capacidad actualizada " + fecha + ": -" + toneladas + " tn ("
                        + numContenedores + " contenedores, " + totalEnvases + " envases)");
                return "OK#NOTIFICACION_RECIBIDA";
            }
            return ERR_CAPACIDAD;
        } catch (DateTimeParseException | NumberFormatException e) {
            return ERR_CAPACIDAD;
        }
    }
}
