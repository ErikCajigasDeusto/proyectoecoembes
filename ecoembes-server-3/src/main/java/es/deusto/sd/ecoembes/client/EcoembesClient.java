package es.deusto.sd.ecoembes.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cliente TCP de Servidor-ContSocket (protocolo: writeUTF/readUTF con delimitador '#').
 * Abre una conexion por peticion: envia un String y espera un String de respuesta.
 *
 */
@Component
public class EcoembesClient {

    private static final String DELIMITER = "#";
    private static final String OK = "OK";

    private final String host;
    private final int port;

    public EcoembesClient(@Value("${contsocket.host:localhost}") String host,
                          @Value("${contsocket.port:8083}") int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * GET_CAPACIDAD#{fecha}
     * @return toneladas disponibles, o vacio si ContSocket responde ERROR (sin registro / formato incorrecto).
     * @throws IOException si falla la comunicacion con el servidor.
     */
    public Optional<Double> getCapacidad(LocalDate fecha) throws IOException {
        String respuesta = enviar("GET_CAPACIDAD" + DELIMITER + fecha);
        String[] partes = respuesta.split(DELIMITER, 2);
        if (partes.length == 2 && OK.equals(partes[0])) {
            try {
                return Optional.of(Double.parseDouble(partes[1]));
            } catch (NumberFormatException e) {
                throw new IOException("Respuesta no valida de ContSocket: " + respuesta, e);
            }
        }
        return Optional.empty();
    }

    /**
     * NOTIFICAR#{fecha}#{numContenedores}#{totalEnvases}
     * @return true si ContSocket responde OK#NOTIFICACION_RECIBIDA; false si responde ERROR.
     * @throws IOException si falla la comunicacion con el servidor.
     */
    public boolean notificar(LocalDate fecha, int numContenedores, int totalEnvases) throws IOException {
        String respuesta = enviar("NOTIFICAR" + DELIMITER + fecha + DELIMITER
                + numContenedores + DELIMITER + totalEnvases);
        return (OK + DELIMITER + "NOTIFICACION_RECIBIDA").equals(respuesta);
    }

    /** Abre el socket, envia la peticion y devuelve la respuesta. */
    private String enviar(String peticion) throws IOException {
        System.out.println(" - EcoembesClient -> " + host + ":" + port + " | " + peticion);
        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            out.writeUTF(peticion);
            out.flush();
            String respuesta = in.readUTF();
            System.out.println(" - EcoembesClient <- " + respuesta);
            return respuesta;
        }
    }
}