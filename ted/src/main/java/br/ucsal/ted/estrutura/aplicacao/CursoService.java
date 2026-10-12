package br.ucsal.ted.estrutura.aplicacao;

import br.ucsal.ted.compartilhado.aplicacao.ServicoCadastral;
import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.dominio.Especificacoes;
import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.estrutura.aplicacao.dto.CursoRequest;
import br.ucsal.ted.estrutura.aplicacao.dto.CursoResponse;
import br.ucsal.ted.estrutura.dominio.CentroAcademico;
import br.ucsal.ted.estrutura.dominio.Curso;
import br.ucsal.ted.estrutura.infra.CentroAcademicoRepository;
import br.ucsal.ted.estrutura.infra.CursoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CursoService extends ServicoCadastral<Curso> {

    private final CursoRepository cursos;
    private final CentroAcademicoRepository centros;

    public CursoService(CursoRepository cursos, CentroAcademicoRepository centros,
                        ApplicationEventPublisher eventos) {
        super(cursos, eventos);
        this.cursos = cursos;
        this.centros = centros;
    }

    @Override
    protected String nomeEntidade() {
        return "Curso";
    }

    @Transactional(readOnly = true)
    public PaginaResponse<CursoResponse> listar(Long centroAcademicoId, Status status, String busca,
                                                Pageable pageable) {
        Specification<Curso> filtro = Especificacoes.<Curso>igual("centroAcademico.id", centroAcademicoId)
                .and(Especificacoes.igual("status", status))
                .and(Especificacoes.contemEmAlgum(busca, "sigla", "descricao"));
        return PaginaResponse.de(cursos.findAll(filtro, pageable), CursoResponse::de);
    }

    @Transactional(readOnly = true)
    public CursoResponse buscar(Long id) {
        return CursoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public CursoResponse cadastrar(CursoRequest request) {
        CentroAcademico ca = centroAtivo(request.centroAcademicoId());
        if (cursos.existsBySiglaIgnoreCaseAndCentroAcademicoId(request.sigla().trim(), ca.getId())) {
            throw new RegraNegocioException("Já existe um curso com a sigla " + request.sigla() + " neste centro acadêmico");
        }
        Curso curso = new Curso(request.sigla(), request.descricao(), request.coordenador(), ca, request.turnos());
        cursos.save(curso);
        registrar(AcaoAuditada.CRIAR, curso, curso.getSigla());
        return CursoResponse.de(curso);
    }

    @Transactional
    public CursoResponse editar(Long id, CursoRequest request) {
        Curso curso = buscarEntidade(id);
        CentroAcademico ca = curso.getCentroAcademico().getId().equals(request.centroAcademicoId())
                ? curso.getCentroAcademico() : centroAtivo(request.centroAcademicoId());
        if (cursos.existsBySiglaIgnoreCaseAndCentroAcademicoIdAndIdNot(request.sigla().trim(), ca.getId(), id)) {
            throw new RegraNegocioException("Já existe um curso com a sigla " + request.sigla() + " neste centro acadêmico");
        }
        curso.atualizar(request.sigla(), request.descricao(), request.coordenador(), ca, request.turnos());
        registrar(AcaoAuditada.EDITAR, curso, curso.getSigla());
        return CursoResponse.de(curso);
    }

    @Override
    protected void antesDeReativar(Curso curso) {
        if (!curso.getCentroAcademico().estaAtiva()) {
            throw new RegraNegocioException(
                    "Reative o centro acadêmico " + curso.getCentroAcademico().getNome() + " antes do curso");
        }
    }

    private CentroAcademico centroAtivo(Long id) {
        CentroAcademico ca = centros.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Centro acadêmico", id));
        if (!ca.estaAtiva()) {
            throw new RegraNegocioException("O centro acadêmico " + ca.getNome() + " está inativo");
        }
        return ca;
    }
}
