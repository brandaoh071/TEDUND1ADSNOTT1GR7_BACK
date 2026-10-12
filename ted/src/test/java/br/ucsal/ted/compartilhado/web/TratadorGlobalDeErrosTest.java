package br.ucsal.ted.compartilhado.web;

import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TratadorGlobalDeErrosTest {

    record Pedido(@NotBlank String sigla) {
    }

    @RestController
    static class ControllerFalso {

        @GetMapping("/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("IES", 99);
        }

        @GetMapping("/regra")
        void regra() {
            throw new RegraNegocioException("A IES já possui um reitor");
        }

        @GetMapping("/negado")
        void negado() {
            throw new AccessDeniedException("negado");
        }

        @GetMapping("/falha")
        void falha() {
            throw new IllegalStateException("detalhe interno");
        }

        @PostMapping("/validacao")
        void validacao(@Valid @RequestBody Pedido pedido) {
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ControllerFalso())
                .setControllerAdvice(new TratadorGlobalDeErros())
                .build();
    }

    @Test
    void recursoNaoEncontradoDevolve404() throws Exception {
        mvc.perform(get("/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("IES com id 99 não encontrado(a)"));
    }

    @Test
    void regraDeNegocioDevolve422() throws Exception {
        mvc.perform(get("/regra"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.title").value("Regra de negócio violada"));
    }

    @Test
    void acessoNegadoDevolve403() throws Exception {
        mvc.perform(get("/negado")).andExpect(status().isForbidden());
    }

    @Test
    void erroInesperadoNaoExpoeDetalhes() throws Exception {
        mvc.perform(get("/falha"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    @Test
    void validacaoDevolve400ComCampos() throws Exception {
        mvc.perform(post("/validacao").contentType(MediaType.APPLICATION_JSON).content("{\"sigla\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("sigla"));
    }
}
