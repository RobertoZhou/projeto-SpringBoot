package br.pucpr.projeto.livros.controller;

import br.pucpr.projeto.livros.dto.CategoriaRequest;
import br.pucpr.projeto.livros.model.Categoria;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import br.pucpr.projeto.livros.service.CategorySuggestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings({"removal","null"})
class CategoriaControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean CategoriaRepository repo;
    @MockBean CategorySuggestionService suggestion;

    @Test @DisplayName("GET /api/categorias lista categorias")
    void list_ok() throws Exception {
        var c = new Categoria("Romance");
        var id = Categoria.class.getDeclaredField("id"); id.setAccessible(true); id.set(c, 3L);
        when(repo.findAll()).thenReturn(List.of(c));

        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].nome").value("Romance"));
    }

    @Test @DisplayName("POST /api/categorias 422 quando nome duplicado (case-insensitive)")
    void create_422_duplicate() throws Exception {
        when(repo.existsByNomeIgnoreCase("Geral")).thenReturn(true);
        var req = new CategoriaRequest("Geral");
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test @DisplayName("POST /api/categorias 201 quando válido")
    void create_201_ok() throws Exception {
        when(repo.existsByNomeIgnoreCase("Tech")).thenReturn(false);
        when(repo.save(any(Categoria.class))).thenAnswer(inv -> {
            var c = inv.getArgument(0, Categoria.class);
            var id = Categoria.class.getDeclaredField("id"); id.setAccessible(true); id.set(c, 8L);
            return c;
        });
        var req = new CategoriaRequest("Tech");
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/categorias/8")))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.nome").value("Tech"));
    }

    @Test @DisplayName("PUT /api/categorias/{id} 200 atualiza nome")
    void update_200() throws Exception {
        var c = new Categoria("Old");
        var id = Categoria.class.getDeclaredField("id"); id.setAccessible(true); id.set(c, 12L);
        when(repo.findById(12L)).thenReturn(Optional.of(c));
        when(repo.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new CategoriaRequest("NewName");
        mvc.perform(put("/api/categorias/12").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.nome").value("NewName"));
    }

    @Test @DisplayName("PUT /api/categorias/{id} 404 quando não existe")
    void update_404() throws Exception {
        when(repo.findById(77L)).thenReturn(Optional.empty());
        var req = new CategoriaRequest("X");
        mvc.perform(put("/api/categorias/77").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("DELETE /api/categorias/{id} 204 quando existe")
    void delete_204() throws Exception {
        when(repo.existsById(5L)).thenReturn(true);
        mvc.perform(delete("/api/categorias/5"))
                .andExpect(status().isNoContent());
        verify(repo).deleteById(5L);
    }

    @Test @DisplayName("DELETE /api/categorias/{id} 404 quando não existe")
    void delete_404() throws Exception {
        when(repo.existsById(6L)).thenReturn(false);
        mvc.perform(delete("/api/categorias/6"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("GET /api/categorias/sugerir?isbn= retorna 200 com categoria sugerida")
    void suggest_ok() throws Exception {
        var c = new Categoria("Sugestão");
        var id = Categoria.class.getDeclaredField("id"); id.setAccessible(true); id.set(c, 42L);
        when(suggestion.suggestOrCreateByIsbn("123"))
                .thenReturn(c);

        mvc.perform(get("/api/categorias/sugerir").param("isbn","123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.nome").value("Sugestão"));
    }
}
