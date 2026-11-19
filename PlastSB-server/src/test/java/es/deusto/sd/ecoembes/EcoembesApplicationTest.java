
package es.deusto.sd.ecoembes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EcoembesApplicationTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Comprueba que la documentación OpenAPI y Swagger UI están disponibles
     * y que se han publicado las rutas principales de Ecoembes.
     */
    @Test
    void servesOpenApiAndSwaggerUi() throws Exception {
        HttpResponse<String> api = get("/v3/api-docs");
        assertEquals(200, api.statusCode());
        assertTrue(api.body().contains("/auth/login"));
        assertTrue(api.body().contains("/auth/logout"));
        assertTrue(api.body().contains("/contenedores"));
        assertTrue(api.body().contains("/plantas/capacidades"));
        assertTrue(api.body().contains("/asignaciones"));
        assertTrue(api.body().contains("/alertas/saturacion"));

        HttpResponse<String> ui = get("/swagger-ui/index.html");
        assertEquals(200, ui.statusCode());
        assertTrue(ui.body().contains("swagger-ui"));
    }

    /**
     * Comprueba que el login rechaza credenciales que no están registradas.
     * AuthService mantiene los empleados en memoria y no contiene usuarios
     * iniciales en el código compartido.
     */
    @Test
    void rejectsUnknownEcoembesCredentials() throws Exception {
        String credentials = "{\"email\":\"empleado@ecoembes.com\",\"password\":\"password123\"}";
        HttpResponse<String> login = post("/auth/login", "application/json", credentials);
        assertEquals(401, login.statusCode());
    }

    /**
     * Comprueba que el login no acepta un cuerpo sin los datos necesarios.
     */
    @Test
    void rejectsIncompleteCredentials() throws Exception {
        assertEquals(401, post("/auth/login", "application/json", "{}").statusCode());
        assertEquals(401, post("/auth/login", "application/json",
                "{\"email\":\"empleado@ecoembes.com\"}").statusCode());
    }

    /**
     * Comprueba que las operaciones protegidas no permiten acceder
     * utilizando un token que no corresponde a una sesión activa.
     */
    @Test
    void rejectsInvalidTokensOnProtectedEndpoints() throws Exception {
        String token = "token=999999999";

        assertEquals(401, get("/alertas/saturacion?" + token).statusCode());
        assertEquals(401, post("/alertas/saturacion/evaluar?" + token,
                "text/plain", "").statusCode());
        assertEquals(401, get("/plantas/capacidades?" + token + "&fecha=2026-11-14").statusCode());
        assertEquals(401, get("/asignaciones?" + token
                + "&idPlanta=1&ini=2026-11-01&fin=2026-11-30").statusCode());
        assertEquals(401, get("/contenedores/1/estado?" + token
                + "&fecha=2026-11-14").statusCode());
    }

    /**
     * Comprueba que una petición de creación de contenedor con un token
     * inválido no llega a ejecutar la operación de creación.
     */
    @Test
    void doesNotCreateContainerWithInvalidToken() throws Exception {
        String container = "{\"direccion\":\"Calle Mayor 10\","
                + "\"codigoPostal\":\"48001\",\"capacidadInicial\":100}";

        HttpResponse<String> response = post(
                "/contenedores?token=999999999",
                "application/json", container);

        assertEquals(401, response.statusCode());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String contentType, String body)
            throws Exception {
        return client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .header("Content-Type", contentType)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }
}
