package es.deusto.sd.ecoembes.facade;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import es.deusto.sd.ecoembes.entity.Personal;
import es.deusto.sd.ecoembes.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerWebTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void loginReturnsTokenAndPersonalIdForValidCredentials() throws Exception {
        Long tokenEsperado = 1731667200000L;
        Personal empleado = new Personal(1L, "Ana García", "ana.garcia@ecoembes.es", "password123");

        when(authService.login("ana.garcia@ecoembes.es", "password123")).thenReturn(tokenEsperado);
        when(authService.validarToken(tokenEsperado)).thenReturn(empleado);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana.garcia@ecoembes.es\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(tokenEsperado))
                .andExpect(jsonPath("$.idPersonal").value(1L));
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        when(authService.login("ana.garcia@ecoembes.es", "invalid"))
                .thenThrow(new IllegalArgumentException("Credenciales incorrectas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana.garcia@ecoembes.es\",\"password\":\"invalid\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutReturnsNoContentForValidToken() throws Exception {
        Long tokenValido = 1731667200000L;
        Personal empleado = new Personal(1L, "Ana García", "ana.garcia@ecoembes.es", "password123");

        when(authService.validarToken(tokenValido)).thenReturn(empleado);
        doNothing().when(authService).logout(tokenValido);

        mockMvc.perform(post("/auth/logout")
                        .param("token", String.valueOf(tokenValido)))
                .andExpect(status().isNoContent());

        verify(authService).logout(tokenValido);
    }

    @Test
    void logoutRejectsUnknownToken() throws Exception {
        Long tokenInvalido = 999999999L;

        doThrow(new SecurityException("Sesión no válida o cerrada"))
                .when(authService).validarToken(tokenInvalido);

        mockMvc.perform(post("/auth/logout")
                        .param("token", String.valueOf(tokenInvalido)))
                .andExpect(status().isUnauthorized());
    }
}