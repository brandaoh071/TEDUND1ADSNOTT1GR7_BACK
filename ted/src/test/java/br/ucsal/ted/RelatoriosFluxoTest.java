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

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class RelatoriosFluxoTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void cadaPerfilEmiteSeuRelatorioComIdentificacaoDoEmissor() throws Exception {
        String admin = login("admin", "admin123");
        Long curso = idDoCurso(admin, "BES");
        Long centro = idDoCentro(admin, "CCET");

        Long docenteInativo = criar(admin, "/api/docentes", """
                {"matricula":"REL-DOC-2","nomeCompleto":"Docente Afastado","email":"rel-doc2@exemplo.br",
                 "telefone":"1","centroAcademicoId":%d,"senha":"senha123"}""".formatted(centro));
        executar(admin, patch("/api/docentes/" + docenteInativo + "/inativar"), "{\"motivo\":\"AFASTAMENTO\"}")
                .andExpect(status().isNoContent());
        criar(admin, "/api/docentes", """
                {"matricula":"REL-DOC-1","nomeCompleto":"Docente Relator","email":"rel-doc1@exemplo.br",
                 "telefone":"1","centroAcademicoId":%d,"senha":"senha123"}""".formatted(centro));
        String colaborador = login("REL-DOC-1", "senha123");

        Long d1 = criar(colaborador, "/api/disciplinas", """
                {"sigla":"REL1","descricao":"Relatórios I","cursoId":%d,"cargaHoraria":60}""".formatted(curso));
        Long d2 = criar(colaborador, "/api/disciplinas", """
                {"sigla":"REL2","descricao":"Relatórios II","cursoId":%d,"cargaHoraria":40}""".formatted(curso));
        criar(colaborador, "/api/discentes", """
                {"matricula":"REL-ALU-1","nomeCompleto":"Aluna Relatora","email":"rel-alu1@exemplo.br",
                 "telefone":"1","cursoId":%d,"senha":"senha123"}""".formatted(curso));
        String discente = login("REL-ALU-1", "senha123");

        Long solicitacao = criar(discente, "/api/solicitacoes", """
                {"itens":[{"disciplinaId":%d,"prioridade":5},{"disciplinaId":%d,"prioridade":2}]}"""
                .formatted(d1, d2));

        mvc.perform(post("/api/relatorios/estrutura").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emissao.tipo").value("ESTRUTURA_ACADEMICA"))
                .andExpect(jsonPath("$.emissao.emitidoPor").value("admin"))
                .andExpect(jsonPath("$.emissao.perfilEmissor").value("ADMIN"))
                .andExpect(jsonPath("$.instituicoes[*].centrosAcademicos[*].cursos[*].sigla", hasItem("BES")));

        mvc.perform(post("/api/relatorios/docentes").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.centrosAcademicos[*].ativos[*].matricula", hasItem("REL-DOC-1")))
                .andExpect(jsonPath("$.centrosAcademicos[*].inativos[*].matricula", hasItem("REL-DOC-2")))
                .andExpect(jsonPath("$.totalInativos", greaterThanOrEqualTo(1)));

        mvc.perform(post("/api/relatorios/docentes").param("status", "ATIVO").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emissao.filtroStatus").value("ATIVO"))
                .andExpect(jsonPath("$.totalInativos").value(0))
                .andExpect(jsonPath("$.centrosAcademicos[*].ativos[*].status", everyItem(is("ATIVO"))));

        mvc.perform(post("/api/relatorios/docentes").header(HttpHeaders.AUTHORIZATION, colaborador))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/solicitacoes/" + solicitacao + "/aprovar").header(HttpHeaders.AUTHORIZATION, colaborador))
                .andExpect(status().isOk());

        mvc.perform(post("/api/relatorios/colaborador").header(HttpHeaders.AUTHORIZATION, colaborador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emissao.perfilEmissor").value("COLABORADOR"))
                .andExpect(jsonPath("$.emissao.nomeEmissor").value("Docente Relator"))
                .andExpect(jsonPath("$.docente.vinculo", containsString("CCET")))
                .andExpect(jsonPath("$.disciplinas[*].sigla", is(List.of("REL1", "REL2"))))
                .andExpect(jsonPath("$.discentes[0].matricula").value("REL-ALU-1"))
                .andExpect(jsonPath("$.solicitacoesValidadas[0].id").value(solicitacao))
                .andExpect(jsonPath("$.solicitacoesValidadas[0].discenteNome").value("Aluna Relatora"));

        mvc.perform(post("/api/relatorios/discente").header(HttpHeaders.AUTHORIZATION, discente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emissao.perfilEmissor").value("USUARIO"))
                .andExpect(jsonPath("$.emissao.nomeEmissor").value("Aluna Relatora"))
                .andExpect(jsonPath("$.discente.vinculo", containsString("BES")))
                .andExpect(jsonPath("$.solicitacoes[0].status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.solicitacoes[0].itens[0].sigla").value("REL1"));

        mvc.perform(post("/api/relatorios/discente").header(HttpHeaders.AUTHORIZATION, colaborador))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/auditoria").param("acao", "EMITIR_RELATORIO").param("usuario", "REL-ALU-1")
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].entidade").value("Relatorio"));
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
        ResultActions resultado = executar(token, post(url), corpo).andExpect(status().isCreated());
        Number id = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
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
}
