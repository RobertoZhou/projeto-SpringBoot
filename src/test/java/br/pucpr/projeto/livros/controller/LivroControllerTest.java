package br.pucpr.projeto.livros.controller;

import br.pucpr.projeto.livros.dto.LivroRequest;
import br.pucpr.projeto.livros.model.Categoria;
import br.pucpr.projeto.livros.model.Livro;
import br.pucpr.projeto.livros.repository.CategoriaRepository;
import br.pucpr.projeto.livros.repository.LivroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LivroController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings({"removal","null"})
class LivroControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean LivroRepository livros;
    @MockBean CategoriaRepository categorias;

    @Test @DisplayName("GET /api/livros deve retornar lista JSON")
    void list_ok() throws Exception {
        var cat = new Categoria("Fantasia");
        var l = new Livro("Titulo", "Autor", cat, new BigDecimal("10.00"), "1234567890123");
        // simular ID e categoria id
        var catField = Categoria.class.getDeclaredField("id"); catField.setAccessible(true); catField.set(cat, 1L);
        var idField = Livro.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(l, 10L);
        when(livros.findAll()).thenReturn(List.of(l));

    mvc.perform(get("/api/livros"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].titulo").value("Titulo"))
        .andExpect(jsonPath("$[0].categoriaId").value(1));
    }

    @Test @DisplayName("POST /api/livros cria e retorna 201")
    void create_201() throws Exception {
        var cat = new Categoria("Fantasia");
        var catField = Categoria.class.getDeclaredField("id"); catField.setAccessible(true); catField.set(cat, 2L);
        when(categorias.findById(2L)).thenReturn(Optional.of(cat));
        when(livros.existsByIsbn("111")) .thenReturn(false);
        when(livros.save(org.mockito.ArgumentMatchers.<Livro>any())).thenAnswer(inv -> {
            Livro li = inv.getArgument(0);
            var idField = Livro.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(li, 99L);
            return li;
        });

        var req = new LivroRequest("Titulo X","Autor Y",2L,new BigDecimal("25.90"),"111",null);
    mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/livros/99")))
        .andExpect(jsonPath("$.id").value(99))
        .andExpect(jsonPath("$.titulo").value("Titulo X"));
    }

    @Test @DisplayName("POST /api/livros retorna 409 se ISBN duplicado")
    void create_409_isbnDuplicado() throws Exception {
        when(categorias.findById(1L)).thenReturn(Optional.of(new Categoria("Geral")));
        when(livros.existsByIsbn("dup")).thenReturn(true);
        var req = new LivroRequest("A","B",1L,new BigDecimal("10.00"),"dup",null);
        mvc.perform(post("/api/livros").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test @DisplayName("GET /api/livros/{id} 404 quando não existe")
    void get_404() throws Exception {
        when(livros.findById(123L)).thenReturn(Optional.empty());
        mvc.perform(get("/api/livros/123"))
                .andExpect(status().isNotFound());
    }

    @Test @DisplayName("PUT /api/livros/{id} 200 quando válido")
    void update_200() throws Exception {
        var catOld = new Categoria("A"); var catNew = new Categoria("B");
        var catField = Categoria.class.getDeclaredField("id"); catField.setAccessible(true); catField.set(catOld, 1L); catField.set(catNew, 2L);
        var livro = new Livro("t","a",catOld,new BigDecimal("5.00"),"aaa");
        var idField = Livro.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(livro, 5L);
        when(livros.findById(5L)).thenReturn(Optional.of(livro));
        when(categorias.findById(2L)).thenReturn(Optional.of(catNew));
        when(livros.existsByIsbn("bbb")).thenReturn(false);

        var req = new LivroRequest("Novo","AutorZ",2L,new BigDecimal("9.90"),"bbb",null);
    mvc.perform(put("/api/livros/5").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo").value("Novo"))
        .andExpect(jsonPath("$.categoriaId").value(2));
    }

    @Test @DisplayName("DELETE /api/livros/{id} 204 quando existe")
    void delete_204() throws Exception {
        when(livros.existsById(7L)).thenReturn(true);
        mvc.perform(delete("/api/livros/7"))
                .andExpect(status().isNoContent());
        verify(livros).deleteById(7L);
    }
}
