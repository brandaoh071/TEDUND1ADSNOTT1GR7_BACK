package br.ucsal.ted.disciplinas.aplicacao;

import br.ucsal.ted.compartilhado.aplicacao.ServicoCadastral;
import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.dominio.Especificacoes;
import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.disciplinas.aplicacao.dto.DisciplinaRequest;
import br.ucsal.ted.disciplinas.aplicacao.dto.DisciplinaResponse;
import br.ucsal.ted.disciplinas.dominio.Disciplina;
import br.ucsal.ted.disciplinas.infra.DisciplinaRepository;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.estrutura.api.EstruturaApi;
import br.ucsal.ted.pessoas.api.DocenteResumo;
import br.ucsal.ted.pessoas.api.PessoasApi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DisciplinaService extends ServicoCadastral<Disciplina> {

    private final DisciplinaRepository disciplinas;
    private final EstruturaApi estrutura;
    private final PessoasApi pessoas;

    public DisciplinaService(DisciplinaRepository disciplinas, EstruturaApi estrutura, PessoasApi pessoas,
                             ApplicationEventPublisher eventos) {
        super(disciplinas, eventos);
        this.disciplinas = disciplinas;
        this.estrutura = estrutura;
        this.pessoas = pessoas;
    }

    @Override
    protected String nomeEntidade() {
        return "Disciplina";
    }

    @Transactional(readOnly = true)
    public PaginaResponse<DisciplinaResponse> listar(Long cursoId, Status status, String busca, boolean minhas,
                                                     Pageable pageable) {
        Long docenteId = minhas ? docenteLogado().map(DocenteResumo::id).orElse(-1L) : null;
        Specification<Disciplina> filtro = Especificacoes.<Disciplina>igual("cursoId", cursoId)
                .and(Especificacoes.igual("status", status))
                .and(Especificacoes.igual("docenteId", docenteId))
                .and(Especificacoes.contemEmAlgum(busca, "sigla", "descricao"));
        Page<Disciplina> pagina = disciplinas.findAll(filtro, pageable);
        Map<Long, CursoResumo> cursos = estrutura.buscarCursos(
                pagina.getContent().stream().map(Disciplina::getCursoId).collect(Collectors.toSet()));
        Map<Long, DocenteResumo> docentes = pessoas.buscarDocentes(
                pagina.getContent().stream().map(Disciplina::getDocenteId).collect(Collectors.toSet()));
        return PaginaResponse.de(pagina,
                d -> DisciplinaResponse.de(d, cursos.get(d.getCursoId()), docentes.get(d.getDocenteId())));
    }

    @Transactional(readOnly = true)
    public DisciplinaResponse buscar(Long id) {
        return resposta(buscarEntidade(id));
    }

    @Transactional
    public DisciplinaResponse cadastrar(DisciplinaRequest request) {
        cursoAtivo(request.cursoId());
        Long docenteId = docenteResponsavel(request.docenteId());
        if (disciplinas.existsBySiglaIgnoreCaseAndCursoId(request.sigla().trim(), request.cursoId())) {
            throw new RegraNegocioException("Já existe uma disciplina com a sigla " + request.sigla() + " neste curso");
        }
        Disciplina disciplina = new Disciplina(request.sigla(), request.descricao(), request.cursoId(),
                request.cargaHoraria(), docenteId);
        disciplinas.save(disciplina);
        registrar(AcaoAuditada.CRIAR, disciplina, disciplina.getSigla());
        return resposta(disciplina);
    }

    @Transactional
    public DisciplinaResponse editar(Long id, DisciplinaRequest request) {
        Disciplina disciplina = buscarEntidade(id);
        verificarPermissao(disciplina);
        if (!disciplina.getCursoId().equals(request.cursoId())) {
            cursoAtivo(request.cursoId());
        }
        if (disciplinas.existsBySiglaIgnoreCaseAndCursoIdAndIdNot(request.sigla().trim(), request.cursoId(), id)) {
            throw new RegraNegocioException("Já existe uma disciplina com a sigla " + request.sigla() + " neste curso");
        }
        Long docenteId = UsuarioLogado.atual().ehAdministrador() && request.docenteId() != null
                ? docenteAtivo(request.docenteId()).id()
                : disciplina.getDocenteId();
        disciplina.atualizar(request.sigla(), request.descricao(), request.cursoId(), request.cargaHoraria(),
                docenteId);
        registrar(AcaoAuditada.EDITAR, disciplina, disciplina.getSigla());
        return resposta(disciplina);
    }

    @Override
    protected void antesDeInativar(Disciplina disciplina) {
        verificarPermissao(disciplina);
    }

    @Override
    protected void antesDeReativar(Disciplina disciplina) {
        verificarPermissao(disciplina);
        cursoAtivo(disciplina.getCursoId());
    }

    private void verificarPermissao(Disciplina disciplina) {
        if (UsuarioLogado.atual().ehAdministrador()) {
            return;
        }
        boolean responsavel = docenteLogado().map(d -> disciplina.lecionadaPor(d.id())).orElse(false);
        if (!responsavel) {
            throw new AccessDeniedException("Somente o docente responsável pode alterar esta disciplina");
        }
    }

    private Long docenteResponsavel(Long docenteInformado) {
        if (docenteInformado != null && UsuarioLogado.atual().ehAdministrador()) {
            return docenteAtivo(docenteInformado).id();
        }
        return docenteLogado()
                .map(DocenteResumo::id)
                .orElseThrow(() -> new RegraNegocioException("Informe o docente responsável pela disciplina"));
    }

    private Optional<DocenteResumo> docenteLogado() {
        return pessoas.buscarDocentePorUsuario(UsuarioLogado.atual().id()).filter(DocenteResumo::ativo);
    }

    private DocenteResumo docenteAtivo(Long id) {
        DocenteResumo docente = pessoas.buscarDocente(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Docente", id));
        if (!docente.ativo()) {
            throw new RegraNegocioException("O docente " + docente.nomeCompleto() + " está inativo");
        }
        return docente;
    }

    private CursoResumo cursoAtivo(Long id) {
        CursoResumo curso = estrutura.buscarCurso(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso", id));
        if (!curso.ativo()) {
            throw new RegraNegocioException("O curso " + curso.sigla() + " está inativo");
        }
        return curso;
    }

    private DisciplinaResponse resposta(Disciplina disciplina) {
        return DisciplinaResponse.de(disciplina,
                estrutura.buscarCurso(disciplina.getCursoId()).orElse(null),
                pessoas.buscarDocente(disciplina.getDocenteId()).orElse(null));
    }
}
