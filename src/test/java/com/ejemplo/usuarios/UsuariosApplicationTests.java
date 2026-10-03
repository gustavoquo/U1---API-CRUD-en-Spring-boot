package com.ejemplo.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuariosApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void publicAuthAndUsersEndpointsWorkWithoutExposingPassword() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nombre":"Ada","email":"ada@example.com","password":"abc1234"}
                """))
        .andExpect(status().isBadRequest());

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nombre":"Ada","email":"ada@example.com","password":"clave123"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.password").doesNotExist());

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"ada@example.com","password":"clave123"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuario.password").doesNotExist());

    mockMvc.perform(get("/api/v1/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].password").doesNotExist());

    mockMvc.perform(get("/api/v1/users/no-es-id"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.mensaje")
            .value("Ocurrió un error interno en el servidor"))
        .andExpect(jsonPath("$.stackTrace").doesNotExist())
        .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
