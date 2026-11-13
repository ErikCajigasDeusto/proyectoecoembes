
package es.deusto.sd.ecoembes.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import es.deusto.sd.ecoembes.entity.Personal;

@Service
public class AuthService {

    // Empleados simulados, guardados en memoria
    private final Map<String, Personal> empleados =
            new ConcurrentHashMap<>();

    // Sesiones activas: token asociado al empleado
    private final Map<Long, Personal> sesionesActivas =
            new ConcurrentHashMap<>();

    // Último token generado
    private static long ultimoToken = 0;

    // Constructor
    public AuthService() {
    }

    // Iniciar sesión
    public Long login(String email, String password) {

        if (email == null || password == null) {
            throw new IllegalArgumentException(
                    "Email o contraseña no válidos"
            );
        }

        Personal empleado = empleados.get(email);

        if (empleado == null ||
                !empleado.comprobarPassword(password)) {
            throw new IllegalArgumentException(
                    "Credenciales incorrectas"
            );
        }

        // Generar el token y guardar la sesión
        Long token = generarToken();
        sesionesActivas.put(token, empleado);

        return token;
    }

    // Cerrar sesión
    public void logout(Long token) {
        if (token != null) {
            sesionesActivas.remove(token);
        }
    }

    // Comprobar un token y obtener su empleado
    public Personal validarToken(Long token) {

        if (token == null) {
            throw new SecurityException("Token no válido");
        }

        Personal empleado = sesionesActivas.get(token);

        if (empleado == null) {
            throw new SecurityException(
                    "Sesión no válida o cerrada"
            );
        }

        return empleado;
    }

    // Añadir empleados de prueba
    public void addPersonal(Personal empleado) {

        if (empleado != null && empleado.getEmail() != null) {
            empleados.putIfAbsent(
                    empleado.getEmail(),
                    empleado
            );
        }
    }

    // Generar un token numérico basado en la hora
    private static synchronized Long generarToken() {

        long ahora = System.currentTimeMillis();

        ultimoToken = Math.max(ahora, ultimoToken + 1);

        return ultimoToken;
    }
}
