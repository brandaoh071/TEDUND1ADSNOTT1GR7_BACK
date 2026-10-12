package br.ucsal.ted.pessoas.aplicacao;

import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.dominio.Especificacoes;
import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.estrutura.api.CentroAcademicoResumo;
import br.ucsal.ted.estrutura.api.EstruturaApi;
import br.ucsal.ted.identidade.api.NomePerfil;
import br.ucsal.ted.identidade.api.UsuarioApi;
import br.ucsal.ted.pessoas.aplicacao.dto.DocenteRequest;
import br.ucsal.ted.pessoas.aplicacao.dto.DocenteResponse;
import br.ucsal.ted.pessoas.dominio.Docente;
import br.ucsal.ted.pessoas.infra.DocenteRepository;
import br.ucsal.ted.pessoas.infra.HistoricoInativacaoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocenteService extends ServicoDePessoa<Docente> {

    private final EstruturaApi estrutura;

    public DocenteService(DocenteRepository docentes, HistoricoInativacaoRepository historicos,
                          UsuarioApi usuarioApi, EstruturaApi estrutura, ApplicationEventPublisher eventos) {
        super(docentes, historicos, usuarioApi, eventos);
        this.estrutura = estrutura;
    }

    @Override
    protected String nomeEntidade() {
        return "Docente";
    }

    @Override
    protected NomePerfil perfilDaConta() {
        return NomePerfil.COLABORADOR;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<DocenteResponse> listar(Long centroAcademicoId, Status status, String busca,
                                                  Pageable pageable) {
        Specification<Docente> filtro = Especificacoes.<Docente>igual("centroAcademicoId", centroAcademicoId)
                .and(Especificacoes.igual("status", status))
                .and(Especificacoes.contemEmAlgum(busca, "nomeCompleto", "matricula", "email"));
        Page<Docente> pagina = pessoas.findAll(filtro, pageable);
        Map<Long, CentroAcademicoResumo> centros = estrutura.buscarCentrosAcademicos(
                pagina.getContent().stream().map(Docente::getCentroAcademicoId).collect(Collectors.toSet()));
        return PaginaResponse.de(pagina, d -> DocenteResponse.de(d, centros.get(d.getCentroAcademicoId())));
    }

    @Transactional(readOnly = true)
    public DocenteResponse buscar(Long id) {
        return resposta(buscarEntidade(id));
    }

    @Transactional
    public DocenteResponse cadastrar(DocenteRequest request) {
        centroAtivo(request.centroAcademicoId());
        Long usuarioId = criarConta(request.matricula(), request.email(), request.senha());
        Docente docente = new Docente(request.matricula(), request.nomeCompleto(), request.email(),
                request.telefone(), request.centroAcademicoId(), usuarioId);
        pessoas.save(docente);
        registrar(AcaoAuditada.CRIAR, docente, docente.getMatricula());
        return resposta(docente);
    }

    @Transactional
    public DocenteResponse editar(Long id, DocenteRequest request) {
        Docente docente = buscarEntidade(id);
        validarEdicao(docente, request.matricula(), request.email());
        if (!docente.getCentroAcademicoId().equals(request.centroAcademicoId())) {
            centroAtivo(request.centroAcademicoId());
        }
        docente.atualizar(request.nomeCompleto(), request.email(), request.telefone(), request.centroAcademicoId());
        registrar(AcaoAuditada.EDITAR, docente, docente.getMatricula());
        return resposta(docente);
    }

    @Override
    protected void antesDeReativar(Docente docente) {
        centroAtivo(docente.getCentroAcademicoId());
    }

    private CentroAcademicoResumo centroAtivo(Long id) {
        CentroAcademicoResumo ca = estrutura.buscarCentroAcademico(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Centro acadêmico", id));
        if (!ca.ativo()) {
            throw new RegraNegocioException("O centro acadêmico " + ca.nome() + " está inativo");
        }
        return ca;
    }

    private DocenteResponse resposta(Docente docente) {
        return DocenteResponse.de(docente, estrutura.buscarCentroAcademico(docente.getCentroAcademicoId()).orElse(null));
    }
}
