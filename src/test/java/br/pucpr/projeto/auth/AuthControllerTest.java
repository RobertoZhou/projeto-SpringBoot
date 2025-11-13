package br.pucpr.projeto.auth;

import br.pucpr.projeto.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void registerOk() throws Exception {
        String json = "{\n" +
                "  \"nome\": \"Teste\",\n" +
                "  \"email\": \"teste@example.com\",\n" +
                "  \"senha\": \"segredo123\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Teste"))
                .andExpect(jsonPath("$.email").value("teste@example.com"));
    }

    @Test
    @DisplayName("Não deve registrar email duplicado")
    void registerDuplicateEmail() throws Exception {
        String json = "{\n" +
                "  \"nome\": \"Teste\",\n" +
                "  \"email\": \"dup@example.com\",\n" +
                "  \"senha\": \"segredo123\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Email já registrado"));
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios")
    void registerValidationErrors() throws Exception {
        String json = "{\n" +
                "  \"nome\": \"\",\n" +
                "  \"email\": \"invalido\",\n" +
                "  \"senha\": \"123\"\n" +
                "}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.fields.nome").exists())
                .andExpect(jsonPath("$.details.fields.email").value("Email inválido"))
                .andExpect(jsonPath("$.details.fields.senha").exists());
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void loginOk() throws Exception {
        String reg = "{\n" +
                "  \"nome\": \"LoginUser\",\n" +
                "  \"email\": \"login@example.com\",\n" +
                "  \"senha\": \"segredo123\"\n" +
                "}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reg))
                .andExpect(status().isCreated());

        String login = "{\n" +
                "  \"email\": \"login@example.com\",\n" +
                "  \"senha\": \"segredo123\"\n" +
                "}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("LoginUser"))
                .andExpect(jsonPath("$.email").value("login@example.com"));
    }

    @Test
    @DisplayName("Deve falhar login com credenciais inválidas")
    void loginInvalido() throws Exception {
        String login = "{\n" +
                "  \"email\": \"naoexiste@example.com\",\n" +
                "  \"senha\": \"errada\"\n" +
                "}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }
}
