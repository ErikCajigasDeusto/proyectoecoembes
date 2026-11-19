package es.deusto.sd.ecoembes.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import es.deusto.sd.ecoembes.entity.Personal;
import es.deusto.sd.ecoembes.service.AuthService;

class AuthControllerWebTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = new AuthService();

        authService.addPersonal(
                new Personal(1L, "Ana García",
                        "ana@ecoembes.com", "Ana1234!"));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .build();
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@ecoembes.com\",\"password\":\"Ana1234!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.idPersonal").value(1));
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@ecoembes.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutReturnsNoContentForValidToken() throws Exception {

        Long token = authService.login(
                "ana@ecoembes.com", "Ana1234!");

        mockMvc.perform(post("/auth/logout")
                .param("token", String.valueOf(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutReturnsUnauthorizedForUnknownToken() throws Exception {

        mockMvc.perform(post("/auth/logout")
                .param("token", "999999999"))
                .andExpect(status().isUnauthorized());
    }
}
