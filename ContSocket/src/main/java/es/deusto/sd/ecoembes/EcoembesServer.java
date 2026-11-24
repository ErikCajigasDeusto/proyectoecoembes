package es.deusto.sd.ecoembes;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import es.deusto.sd.ecoembes.service.AsignacionService;
import es.deusto.sd.ecoembes.service.ContenedorService;
import es.deusto.sd.ecoembes.service.PlantaService;
import es.deusto.sd.ecoembes.service.AuthService;
import es.deusto.sd.ecoembes.service.AlertaService;

public class EcoembesServer {
	private static final AtomicInteger numClients = new AtomicInteger(0);
	private static final ExecutorService pool = Executors.newCachedThreadPool();

	@FunctionalInterface
	interface ServiceFactory {
		Runnable create(Socket socket) throws IOException;
	}
	private static final Map<String, ServiceFactory> SERVICES = new HashMap<>();
	static {
		SERVICES.put("PLANTA",     socket -> new PlantaService(socket));
		SERVICES.put("CONTENEDOR", socket -> new ContenedorService(socket, new AlertaService(socket)));
		SERVICES.put("ALERTA",     socket -> new AlertaService(socket));
		SERVICES.put("ASIGNACION", socket -> new AsignacionService(socket));
		SERVICES.put("AUTH",       socket -> new AuthService(socket));
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println(" # Usage: EcoembesServer [PORT]");
			System.exit(1);
		}

		int serverPort = Integer.parseInt(args[0]);

		try (ServerSocket tcpServerSocket = new ServerSocket(serverPort)) {
			System.out.println(" - Ecoembes: Waiting for connections '" + tcpServerSocket.getInetAddress().getHostAddress()
					+ ":" + tcpServerSocket.getLocalPort() + "' ...");
			System.out.println(" - Servicios disponibles: " + SERVICES.keySet());

			while (true) {
				Socket clientSocket = tcpServerSocket.accept();
				System.out.println(" - EcoembesServer: New client connection accepted. Client number: "
						+ numClients.incrementAndGet());
				pool.execute(() -> dispatch(clientSocket));
			}
		} catch (IOException e) {
			System.err.println("# EcoembesServer: IO error:" + e.getMessage());
		} finally {
			pool.shutdown();
		}
	}

	// Lee el nombre del servicio y delega en la clase correspondiente
	private static void dispatch(Socket clientSocket) {
		try {
			// No cerrar este stream: cerraría el socket
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			String serviceName = in.readUTF().trim().toUpperCase();

			Function<Socket, Runnable> factory = (Function<Socket, Runnable>) SERVICES.get(serviceName);
			if (factory == null) {
				System.err.println("# EcoembesServer: servicio desconocido '" + serviceName + "'");
				clientSocket.close();
				return;
			}

			System.out.println(" - EcoembesServer: cliente -> " + serviceName);
			factory.apply(clientSocket).run(); // ya estamos en un hilo del pool
		} catch (IOException e) {
			System.err.println("# EcoembesServer: error en dispatch: " + e.getMessage());
			try { clientSocket.close(); } catch (IOException ignored) {}
		}
	}
}