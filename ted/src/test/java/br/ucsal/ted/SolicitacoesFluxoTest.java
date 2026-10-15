package br.ucsal.ted;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SolicitacoesFluxoTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void discenteSolicitaEColaboradorValidaPorOrdemDeRecebimento() throws Exception {
        String admin = login("admin", "admin123");

        Long cursoCcet = idDoCurso(admin, "BES");
        Long cursoCtc = idDoCurso(admin, "CC");
        Long caCcet = idDoCentro(admin, "CCET");
        Long caCtc = idDoCentro(admin, "CTC");

        criar(admin, "/api/docentes", """
                {"matricula":"SOL-DOC-1","nomeCompleto":"Docente CCET","email":"sol-doc1@exemplo.br","telefone":"1",
                 "centroAcademicoId":%d,"senha":"senha123"}""".formatted(caCcet));
        criar(admin, "/api/docentes", """
                {"matricula":"SOL-DOC-2","nomeCompleto":"Docente CTC","email":"sol-doc2@exemplo.br","telefone":"1",
                 "centroAcademicoId":%d,"senha":"senha123"}""".formatted(caCtc));
        String colaboradorCcet = login("SOL-DOC-1", "senha123");
        String colaboradorCtc = login("SOL-DOC-2", "senha123");

        List<Long> disciplinas = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            disciplinas.add(criar(colaboradorCcet, "/api/disciplinas", """
                    {"sigla":"SOL%d","descricao":"Disciplina %d","cursoId":%d,"cargaHoraria":60}"""
                    .formatted(i, i, cursoCcet)));
        }

        criar(colaboradorCcet, "/api/discentes", """
                {"matricula":"SOL-ALU-1","nomeCompleto":"Aluno Primeiro","email":"sol-alu1@exemplo.br","telefone":"1",
                 "cursoId":%d,"senha":"senha123"}""".formatted(cursoCcet));
        criar(colaboradorCcet, "/api/discentes", """
                {"matricula":"SOL-ALU-2","nomeCompleto":"Aluno Segundo","email":"sol-alu2@exemplo.br","telefone":"1",
                 "cursoId":%d,"senha":"senha123"}""".formatted(cursoCcet));
        criar(colaboradorCtc, "/api/discentes", """
                {"matricula":"SOL-ALU-3","nomeCompleto":"Aluno CTC","email":"sol-alu3@exemplo.br","telefone":"1",
                 "cursoId":%d,"senha":"senha123"}""".formatted(cursoCtc));
        String aluno1 = login("SOL-ALU-1", "senha123");
        String aluno2 = login("SOL-ALU-2", "senha123");
        String aluno3 = login("SOL-ALU-3", "senha123");

        mvc.perform(get("/api/solicitacoes/semestre").header(HttpHeaders.AUTHORIZATION, aluno1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jaSolicitado").value(false));

        executar(aluno1, post("/api/solicitacoes"), itens(disciplinas.get(0), 5))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes").isArray());

        executar(aluno1, post("/api/solicitacoes"), """
                {"itens":[{"disciplinaId":%d,"prioridade":5},{"disciplinaId":%d,"prioridade":5},
                          {"disciplinaId":%d,"prioridade":5}]}"""
                .formatted(disciplinas.get(0), disciplinas.get(1), disciplinas.get(2)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.violacoes[0]").value(org.hamcrest.Matchers.containsString("prioridade máxima")));

        executar(aluno1, post("/api/solicitacoes"), """
                {"itens":[{"disciplinaId":%d,"prioridade":5},{"disciplinaId":99999,"prioridade":2}]}"""
                .formatted(disciplinas.get(0)))
                .andExpect(status().isUnprocessableContent());

        Long primeira = id(executar(aluno1, post("/api/solicitacoes"), """
                {"itens":[{"disciplinaId":%d,"prioridade":5},{"disciplinaId":%d,"prioridade":3},
                          {"disciplinaId":%d,"prioridade":1}]}"""
                .formatted(disciplinas.get(0), disciplinas.get(1), disciplinas.get(2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO"))
                .andExpect(jsonPath("$.itens[0].prioridade").value(5)));

        executar(aluno1, post("/api/solicitacoes"), itens2(disciplinas))
                .andExpect(status().isUnprocessableContent());

        Long segunda = id(executar(aluno2, post("/api/solicitacoes"), itens2(disciplinas))
                .andExpect(status().isCreated()));

        Long terceira = id(executar(aluno3, post("/api/solicitacoes"), itens2(disciplinas))
                .andExpect(status().isCreated()));

        executar(colaboradorCcet, post("/api/solicitacoes"), itens2(disciplinas)).andExpect(status().isForbidden());

        mvc.perform(get("/api/solicitacoes").param("status", "AGUARDANDO")
                        .header(HttpHeaders.AUTHORIZATION, colaboradorCcet))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].id").value(primeira))
                .andExpect(jsonPath("$.conteudo[0].proximaDaFila").value(true))
                .andExpect(jsonPath("$.conteudo[1].id").value(segunda))
                .andExpect(jsonPath("$.conteudo[1].proximaDaFila").value(false))
                .andExpect(jsonPath("$.conteudo[*].id", org.hamcrest.Matchers.not(hasItem(terceira.intValue()))));

        mvc.perform(get("/api/solicitacoes/" + terceira).header(HttpHeaders.AUTHORIZATION, colaboradorCcet))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/solicitacoes/" + terceira + "/aprovar").header(HttpHeaders.AUTHORIZATION, colaboradorCcet))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/solicitacoes/" + segunda + "/aprovar").header(HttpHeaders.AUTHORIZATION, colaboradorCcet))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("ordem de recebimento")));

        mvc.perform(patch("/api/solicitacoes/" + primeira + "/aprovar").header(HttpHeaders.AUTHORIZATION, colaboradorCcet))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.validadoPor").value("SOL-DOC-1"));

        executar(colaboradorCcet, patch("/api/solicitacoes/" + segunda + "/lista-espera"),
                "{\"observacao\":\"Sem vaga na turma\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LISTA_ESPERA"));

        mvc.perform(patch("/api/solicitacoes/" + terceira + "/aprovar").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk());

        mvc.perform(get("/api/solicitacoes/minhas").header(HttpHeaders.AUTHORIZATION, aluno2))
                .andExpect(jsonPath("$.conteudo[0].status").value("LISTA_ESPERA"))
                .andExpect(jsonPath("$.conteudo[0].observacao").value("Sem vaga na turma"));
        mvc.perform(get("/api/solicitacoes/" + primeira).header(HttpHeaders.AUTHORIZATION, aluno2))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/solicitacoes/semestre").header(HttpHeaders.AUTHORIZATION, aluno1))
                .andExpect(jsonPath("$.jaSolicitado").value(true));

        mvc.perform(get("/api/auditoria").param("acao", "VALIDAR_SOLICITACAO").param("usuario", "SOL-")
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get("/api/auditoria").param("acao", "ENVIAR_SOLICITACAO").param("usuario", "SOL-")
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.totalElementos").value(3));
    }

    private String itens(Long disciplina, int prioridade) {
        return "{\"itens\":[{\"disciplinaId\":%d,\"prioridade\":%d}]}".formatted(disciplina, prioridade);
    }

    private String itens2(List<Long> disciplinas) {
        return "{\"itens\":[{\"disciplinaId\":%d,\"prioridade\":4},{\"disciplinaId\":%d,\"prioridade\":2}]}"
                .formatted(disciplinas.get(2), disciplinas.get(3));
    }

    private Long idDoCurso(String token, String sigla) throws Exception {
        String corpo = mvc.perform(get("/api/cursos").param("busca", sigla).param("size", "50")
                .header(HttpHeaders.AUTHORIZATION, token)).andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(corpo, "$.conteudo[?(@.sigla == '" + sigla + "')].id");
        return ids.getFirst().longValue();
    }

    private Long idDoCentro(String token, String sigla) throws Exception {
        String corpo = mvc.perform(get("/api/centros-academicos").param("busca", "(" + sigla + ")")
                .header(HttpHeaders.AUTHORIZATION, token)).andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(corpo, "$.conteudo[*].id");
        return ids.getFirst().longValue();
    }

    private Long criar(String token, String url, String corpo) throws Exception {
        return id(executar(token, post(url), corpo).andExpect(status().isCreated()));
    }

    private String login(String usuario, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(usuario, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(corpo, "$.token");
    }

    private ResultActions executar(String token, MockHttpServletRequestBuilder requisicao, String corpo)
            throws Exception {
        return mvc.perform(requisicao.header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Long id(ResultActions resultado) throws Exception {
        Number id = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
