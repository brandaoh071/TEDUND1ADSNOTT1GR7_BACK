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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class FluxoCompletoTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void fluxoDoAdministradorAteODocente() throws Exception {
        mvc.perform(get("/api/ies")).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/api/auth/login"), "{\"login\":\"admin\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());

        String admin = login("admin", "admin123");

        mvc.perform(get("/api/ies").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));

        Long iesId = id(executar(admin, post("/api/ies"), """
                {"sigla":"UCB","nome":"Universidade Católica de Brasília","emailCentral":"ucb@exemplo.br",
                 "telefoneCentral":"(61) 0000-0000","reitor":"Reitor UCB",
                 "proReitorias":[{"nome":"Graduação","nomeado":"Pró-Reitor A"}]}
                """).andExpect(status().isCreated()));

        executar(admin, post("/api/ies"), """
                {"sigla":"ucb","nome":"Outra","emailCentral":"x@exemplo.br","telefoneCentral":"1","reitor":"R"}
                """).andExpect(status().isUnprocessableContent());

        executar(admin, post("/api/ies"), "{\"sigla\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());

        Long caId = id(executar(admin, post("/api/centros-academicos"), """
                {"nome":"Escola de Tecnologia","emailCentral":"tec@exemplo.br","telefoneCentral":"1",
                 "coordenador":"Coord","iesId":%d}
                """.formatted(iesId)).andExpect(status().isCreated()));

        Long cursoId = id(executar(admin, post("/api/cursos"), """
                {"sigla":"SI","descricao":"Sistemas de Informação","coordenador":"Coord SI",
                 "centroAcademicoId":%d,"turnos":["NOTURNO","MATUTINO"]}
                """.formatted(caId)).andExpect(status().isCreated()));

        mvc.perform(patch("/api/ies/" + iesId + "/inativar").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isUnprocessableContent());

        Long docenteId = id(executar(admin, post("/api/docentes"), """
                {"matricula":"DOC-1","nomeCompleto":"Docente Um","email":"doc1@exemplo.br","telefone":"1",
                 "centroAcademicoId":%d,"senha":"senha123"}
                """.formatted(caId)).andExpect(status().isCreated()));

        String docente = login("DOC-1", "senha123");

        executar(docente, post("/api/ies"), """
                {"sigla":"PUC-SP","nome":"PUC-SP","emailCentral":"p@exemplo.br","telefoneCentral":"1","reitor":"R"}
                """).andExpect(status().isForbidden());

        executar(docente, post("/api/disciplinas"), """
                {"sigla":"ARQ","descricao":"Arquitetura de Software","cursoId":%d,"cargaHoraria":60}
                """.formatted(cursoId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.docenteNome").value("Docente Um"));

        executar(docente, post("/api/discentes"), """
                {"matricula":"ALU-1","nomeCompleto":"Aluno Um","email":"alu1@exemplo.br","telefone":"1",
                 "cursoId":%d,"senha":"senha123"}
                """.formatted(cursoId)).andExpect(status().isCreated());

        String discente = login("ALU-1", "senha123");
        mvc.perform(get("/api/disciplinas").header(HttpHeaders.AUTHORIZATION, discente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1));
        mvc.perform(get("/api/discentes").header(HttpHeaders.AUTHORIZATION, discente))
                .andExpect(status().isForbidden());

        executar(admin, patch("/api/docentes/" + docenteId + "/inativar"),
                "{\"motivo\":\"DESISTENCIA\"}").andExpect(status().isUnprocessableContent());
        executar(admin, patch("/api/docentes/" + docenteId + "/inativar"),
                "{\"motivo\":\"AFASTAMENTO\",\"descricao\":\"Licença\"}").andExpect(status().isNoContent());

        mvc.perform(get("/api/docentes/" + docenteId + "/historico").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].motivo").value("AFASTAMENTO"));

        mvc.perform(json(post("/api/auth/login"), "{\"login\":\"DOC-1\",\"senha\":\"senha123\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/auditoria").param("entidade", "IES").param("acao", "CRIAR")
                        .header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].usuario").value("admin"));
        mvc.perform(get("/api/auditoria").param("acao", "LOGIN_FALHO").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(jsonPath("$.totalElementos").value(2));
    }

    @Test
    void corsAceitaSomenteOFront() throws Exception {
        mvc.perform(options("/api/ies").header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
        mvc.perform(options("/api/ies").header(HttpHeaders.ORIGIN, "http://malicioso.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    private String login(String usuario, String senha) throws Exception {
        String corpo = mvc.perform(json(post("/api/auth/login"),
                        "{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(usuario, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(corpo, "$.token");
    }

    private ResultActions executar(String token, MockHttpServletRequestBuilder requisicao, String corpo)
            throws Exception {
        return mvc.perform(json(requisicao.header(HttpHeaders.AUTHORIZATION, token), corpo));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder requisicao, String corpo) {
        return requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private Long id(ResultActions resultado) throws Exception {
        Number id = JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
