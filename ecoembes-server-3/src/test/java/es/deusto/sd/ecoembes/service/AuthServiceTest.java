package es.deusto.sd.ecoembes.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.deusto.sd.ecoembes.entity.Personal;

class AuthServiceTest {

    private static final String EMAIL = "ana.garcia@ecoembes.es";
    private static final String PASSWORD = "password123";

    private AuthService service;
    private Personal empleado;

    @BeforeEach
    void setUp() {
        service = new AuthService();
        empleado = new Personal(1L, "Ana García", EMAIL, PASSWORD);
        service.addPersonal(empleado);
    }

    @Test
    void logsInWithValidCredentialsAndInvalidatesTokenOnLogout() {
        Long token = service.login(EMAIL, PASSWORD);

        assertNotNull(token);
        assertSame(empleado, service.validarToken(token));

        service.logout(token);

        assertThrows(SecurityException.class, () -> service.validarToken(token));
    }

    @Test
    void generatesDistinctTokensForConsecutiveLogins() {
        Long token1 = service.login(EMAIL, PASSWORD);
        Long token2 = service.login(EMAIL, PASSWORD);

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void rejectsUnknownUsersAndIncorrectPasswords() {
        assertThrows(IllegalArgumentException.class,
                () -> service.login(EMAIL, "wrong-password"));
        assertThrows(IllegalArgumentException.class,
                () -> service.login("desconocido@ecoembes.es", PASSWORD));
    }

    @Test
    void rejectsMissingEmailOrPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.login(null, PASSWORD));
        assertThrows(IllegalArgumentException.class,
                () -> service.login(EMAIL, null));
    }

    @Test
    void rejectsNullOrUnknownTokensAndHandlesUnknownLogoutSafely() {
        assertThrows(SecurityException.class, () -> service.validarToken(null));
        assertThrows(SecurityException.class, () -> service.validarToken(999999999L));

        assertDoesNotThrow(() -> service.logout(null));
        assertDoesNotThrow(() -> service.logout(999999999L));
    }
}