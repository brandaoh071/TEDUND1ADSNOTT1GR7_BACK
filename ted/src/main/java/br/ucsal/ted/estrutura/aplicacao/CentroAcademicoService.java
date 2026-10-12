package br.ucsal.ted.estrutura.aplicacao;

import br.ucsal.ted.compartilhado.aplicacao.ServicoCadastral;
import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.dominio.Especificacoes;
import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.estrutura.aplicacao.dto.CentroAcademicoRequest;
import br.ucsal.ted.estrutura.aplicacao.dto.CentroAcademicoResponse;
import br.ucsal.ted.estrutura.dominio.CentroAcademico;
import br.ucsal.ted.estrutura.dominio.Ies;
import br.ucsal.ted.estrutura.infra.CentroAcademicoRepository;
import br.ucsal.ted.estrutura.infra.CursoRepository;
import br.ucsal.ted.estrutura.infra.IesRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroAcademicoService extends ServicoCadastral<CentroAcademico> {

    private final CentroAcademicoRepository centros;
    private final IesRepository iesRepository;
    private final CursoRepository cursos;

    public CentroAcademicoService(CentroAcademicoRepository centros, IesRepository iesRepository,
                                  CursoRepository cursos, ApplicationEventPublisher eventos) {
        super(centros, eventos);
        this.centros = centros;
        this.iesRepository = iesRepository;
        this.cursos = cursos;
    }

    @Override
    protected String nomeEntidade() {
        return "CentroAcademico";
    }

    @Transactional(readOnly = true)
    public PaginaResponse<CentroAcademicoResponse> listar(Long iesId, Status status, String busca,
                                                          Pageable pageable) {
        Specification<CentroAcademico> filtro = Especificacoes.<CentroAcademico>igual("ies.id", iesId)
                .and(Especificacoes.igual("status", status))
                .and(Especificacoes.contemEmAlgum(busca, "nome", "coordenador"));
        return PaginaResponse.de(centros.findAll(filtro, pageable), CentroAcademicoResponse::de);
    }

    @Transactional(readOnly = true)
    public CentroAcademicoResponse buscar(Long id) {
        return CentroAcademicoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public CentroAcademicoResponse cadastrar(CentroAcademicoRequest request) {
        Ies ies = iesAtiva(request.iesId());
        if (centros.existsByNomeIgnoreCaseAndIesId(request.nome().trim(), ies.getId())) {
            throw new RegraNegocioException("Já existe um centro acadêmico com este nome nesta IES");
        }
        CentroAcademico ca = new CentroAcademico(request.nome(), request.emailCentral(), request.telefoneCentral(),
                request.coordenador(), ies);
        centros.save(ca);
        registrar(AcaoAuditada.CRIAR, ca, ca.getNome());
        return CentroAcademicoResponse.de(ca);
    }

    @Transactional
    public CentroAcademicoResponse editar(Long id, CentroAcademicoRequest request) {
        CentroAcademico ca = buscarEntidade(id);
        Ies ies = ca.getIes().getId().equals(request.iesId()) ? ca.getIes() : iesAtiva(request.iesId());
        if (centros.existsByNomeIgnoreCaseAndIesIdAndIdNot(request.nome().trim(), ies.getId(), id)) {
            throw new RegraNegocioException("Já existe um centro acadêmico com este nome nesta IES");
        }
        ca.atualizar(request.nome(), request.emailCentral(), request.telefoneCentral(), request.coordenador(), ies);
        registrar(AcaoAuditada.EDITAR, ca, ca.getNome());
        return CentroAcademicoResponse.de(ca);
    }

    @Override
    protected void antesDeInativar(CentroAcademico ca) {
        if (cursos.existsByCentroAcademicoIdAndStatus(ca.getId(), Status.ATIVO)) {
            throw new RegraNegocioException(
                    "Não é possível inativar o centro acadêmico enquanto houver cursos ativos vinculados a ele");
        }
    }

    @Override
    protected void antesDeReativar(CentroAcademico ca) {
        if (!ca.getIes().estaAtiva()) {
            throw new RegraNegocioException("Reative a IES " + ca.getIes().getSigla() + " antes do centro acadêmico");
        }
    }

    private Ies iesAtiva(Long iesId) {
        Ies ies = iesRepository.findById(iesId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("IES", iesId));
        if (!ies.estaAtiva()) {
            throw new RegraNegocioException("A IES " + ies.getSigla() + " está inativa");
        }
        return ies;
    }
}
