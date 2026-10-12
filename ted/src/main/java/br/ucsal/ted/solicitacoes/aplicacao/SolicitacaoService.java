package br.ucsal.ted.solicitacoes.aplicacao;

import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.auditoria.EventoAuditoria;
import br.ucsal.ted.compartilhado.dominio.Especificacoes;
import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.excecao.RegrasVioladasException;
import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.disciplinas.api.DisciplinaResumo;
import br.ucsal.ted.disciplinas.api.DisciplinasApi;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.estrutura.api.EstruturaApi;
import br.ucsal.ted.pessoas.api.DiscenteResumo;
import br.ucsal.ted.pessoas.api.DocenteResumo;
import br.ucsal.ted.pessoas.api.PessoasApi;
import br.ucsal.ted.solicitacoes.aplicacao.dto.ItemSolicitacaoRequest;
import br.ucsal.ted.solicitacoes.aplicacao.dto.SemestreResponse;
import br.ucsal.ted.solicitacoes.aplicacao.dto.SolicitacaoRequest;
import br.ucsal.ted.solicitacoes.aplicacao.dto.SolicitacaoResponse;
import br.ucsal.ted.solicitacoes.dominio.ItemSolicitado;
import br.ucsal.ted.solicitacoes.dominio.SemestreReferencia;
import br.ucsal.ted.solicitacoes.dominio.Solicitacao;
import br.ucsal.ted.solicitacoes.dominio.StatusSolicitacao;
import br.ucsal.ted.solicitacoes.dominio.regras.ContextoSolicitacao;
import br.ucsal.ted.solicitacoes.dominio.regras.RegraSolicitacao;
import br.ucsal.ted.solicitacoes.infra.SolicitacaoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SolicitacaoService {

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(BRASILIA);
    private static final Sort ORDEM_DE_RECEBIMENTO = Sort.by("dataSolicitacao", "id");

    private final SolicitacaoRepository solicitacoes;
    private final List<RegraSolicitacao> regras;
    private final PessoasApi pessoas;
    private final DisciplinasApi disciplinas;
    private final EstruturaApi estrutura;
    private final MontadorDeRespostas montador;
    private final ApplicationEventPublisher eventos;

    public SolicitacaoService(SolicitacaoRepository solicitacoes, List<RegraSolicitacao> regras,
                              PessoasApi pessoas, DisciplinasApi disciplinas, EstruturaApi estrutura,
                              MontadorDeRespostas montador, ApplicationEventPublisher eventos) {
        this.solicitacoes = solicitacoes;
        this.regras = regras;
        this.pessoas = pessoas;
        this.disciplinas = disciplinas;
        this.estrutura = estrutura;
        this.montador = montador;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public SemestreResponse semestreAtual() {
        String semestre = semestreReferencia();
        DiscenteResumo discente = discenteLogado();
        return new SemestreResponse(semestre,
                solicitacoes.existsByDiscenteIdAndSemestreReferencia(discente.id(), semestre));
    }

    @Transactional
    public SolicitacaoResponse enviar(SolicitacaoRequest request) {
        DiscenteResumo discente = discenteLogado();
        CursoResumo curso = estrutura.buscarCurso(discente.cursoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Curso", discente.cursoId()));
        String semestre = semestreReferencia();
        List<ItemSolicitado> itens = request.itens().stream()
                .map(i -> new ItemSolicitado(i.disciplinaId(), i.prioridade()))
                .toList();
        Map<Long, Boolean> disciplinasAtivas = disciplinas.buscarTodas(
                        request.itens().stream().map(ItemSolicitacaoRequest::disciplinaId).collect(Collectors.toSet()))
                .values().stream()
                .collect(Collectors.toMap(DisciplinaResumo::id, DisciplinaResumo::ativa));
        ContextoSolicitacao contexto = new ContextoSolicitacao(itens, disciplinasAtivas,
                solicitacoes.existsByDiscenteIdAndSemestreReferencia(discente.id(), semestre), semestre);

        List<String> violacoes = regras.stream()
                .map(regra -> regra.verificar(contexto))
                .flatMap(Optional::stream)
                .toList();
        if (!violacoes.isEmpty()) {
            throw new RegrasVioladasException(violacoes);
        }

        Solicitacao solicitacao = solicitacoes.save(
                new Solicitacao(discente.id(), curso.id(), curso.centroAcademicoId(), semestre, itens));
        publicar(AcaoAuditada.ENVIAR_SOLICITACAO, solicitacao,
                "Semestre " + semestre + " com " + itens.size() + " disciplinas");
        return montador.montar(solicitacao, false);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<SolicitacaoResponse> minhas(Pageable pageable) {
        DiscenteResumo discente = discenteLogado();
        Pageable ordenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "dataSolicitacao"));
        return pagina(solicitacoes.findByDiscenteId(discente.id(), ordenado));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<SolicitacaoResponse> fila(StatusSolicitacao status, String semestre, Pageable pageable) {
        Long centroAcademicoId = centroAcademicoDoValidador().orElse(null);
        Specification<Solicitacao> filtro = Especificacoes.<Solicitacao>igual("status", status)
                .and(Especificacoes.igual("semestreReferencia", semestre == null || semestre.isBlank() ? null : semestre))
                .and(Especificacoes.igual("centroAcademicoId", centroAcademicoId));
        Pageable ordenado = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ORDEM_DE_RECEBIMENTO);
        return pagina(solicitacoes.findAll(filtro, ordenado));
    }

    @Transactional(readOnly = true)
    public SolicitacaoResponse buscar(Long id) {
        Solicitacao solicitacao = buscarEntidade(id);
        UsuarioLogado usuario = UsuarioLogado.atual();
        boolean dono = usuario.possuiPerfil("USUARIO") && pessoas.buscarDiscentePorUsuario(usuario.id())
                .map(d -> d.id().equals(solicitacao.getDiscenteId()))
                .orElse(false);
        if (!dono) {
            verificarPermissaoDeValidacao(solicitacao);
        }
        return montador.montar(solicitacao, proximasDaFila(List.of(solicitacao)).contains(id));
    }

    @Transactional
    public SolicitacaoResponse aprovar(Long id) {
        Solicitacao solicitacao = solicitacaoParaValidar(id);
        solicitacao.aprovar(UsuarioLogado.atual().login());
        publicar(AcaoAuditada.VALIDAR_SOLICITACAO, solicitacao, "Aprovada: status EM_ANALISE");
        return montador.montar(solicitacao, false);
    }

    @Transactional
    public SolicitacaoResponse enviarParaListaDeEspera(Long id, String observacao) {
        Solicitacao solicitacao = solicitacaoParaValidar(id);
        solicitacao.enviarParaListaDeEspera(UsuarioLogado.atual().login(), observacao);
        publicar(AcaoAuditada.VALIDAR_SOLICITACAO, solicitacao,
                "Enviada para LISTA_ESPERA" + (observacao == null || observacao.isBlank() ? "" : ": " + observacao));
        return montador.montar(solicitacao, false);
    }

    private Solicitacao solicitacaoParaValidar(Long id) {
        Solicitacao solicitacao = buscarEntidade(id);
        verificarPermissaoDeValidacao(solicitacao);
        if (solicitacao.getStatus().pendenteDeValidacao()) {
            primeiraDaFila(solicitacao.getCentroAcademicoId(), solicitacao.getStatus())
                    .filter(primeira -> !primeira.getId().equals(solicitacao.getId()))
                    .ifPresent(primeira -> {
                        throw new RegraNegocioException("As solicitações são validadas por ordem de recebimento. "
                                + "Valide antes a solicitação nº " + primeira.getId() + ", recebida em "
                                + FORMATO_DATA.format(primeira.getDataSolicitacao()));
                    });
        }
        return solicitacao;
    }

    private void verificarPermissaoDeValidacao(Solicitacao solicitacao) {
        UsuarioLogado usuario = UsuarioLogado.atual();
        if (usuario.ehAdministrador()) {
            return;
        }
        boolean mesmoCentro = usuario.possuiPerfil("COLABORADOR") && centroAcademicoDoValidador()
                .map(ca -> ca.equals(solicitacao.getCentroAcademicoId()))
                .orElse(false);
        if (!mesmoCentro) {
            throw new AccessDeniedException("Solicitação de outro centro acadêmico");
        }
    }

    private Optional<Long> centroAcademicoDoValidador() {
        UsuarioLogado usuario = UsuarioLogado.atual();
        if (usuario.ehAdministrador()) {
            return Optional.empty();
        }
        return Optional.of(pessoas.buscarDocentePorUsuario(usuario.id())
                .filter(DocenteResumo::ativo)
                .map(DocenteResumo::centroAcademicoId)
                .orElseThrow(() -> new AccessDeniedException("Somente docentes ativos validam solicitações")));
    }

    private DiscenteResumo discenteLogado() {
        return pessoas.buscarDiscentePorUsuario(UsuarioLogado.atual().id())
                .filter(DiscenteResumo::ativo)
                .orElseThrow(() -> new AccessDeniedException("Somente discentes ativos podem solicitar disciplinas"));
    }

    private Solicitacao buscarEntidade(Long id) {
        return solicitacoes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Solicitação", id));
    }

    private Optional<Solicitacao> primeiraDaFila(Long centroAcademicoId, StatusSolicitacao status) {
        return solicitacoes.findFirstByCentroAcademicoIdAndStatusOrderByDataSolicitacaoAscIdAsc(
                centroAcademicoId, status);
    }

    private Set<Long> proximasDaFila(Collection<Solicitacao> lista) {
        return lista.stream()
                .filter(s -> s.getStatus().pendenteDeValidacao())
                .map(s -> new FilaDoCentro(s.getCentroAcademicoId(), s.getStatus()))
                .distinct()
                .map(fila -> primeiraDaFila(fila.centroAcademicoId(), fila.status()))
                .flatMap(Optional::stream)
                .map(Solicitacao::getId)
                .collect(Collectors.toSet());
    }

    private record FilaDoCentro(Long centroAcademicoId, StatusSolicitacao status) {
    }

    private PaginaResponse<SolicitacaoResponse> pagina(Page<Solicitacao> pagina) {
        Map<Long, SolicitacaoResponse> respostas = montador.montarTodas(pagina.getContent(),
                        proximasDaFila(pagina.getContent())).stream()
                .collect(Collectors.toMap(SolicitacaoResponse::id, Function.identity()));
        return PaginaResponse.de(pagina, s -> respostas.get(s.getId()));
    }

    private String semestreReferencia() {
        return SemestreReferencia.subsequente(LocalDate.now(BRASILIA));
    }

    private void publicar(AcaoAuditada acao, Solicitacao solicitacao, String detalhes) {
        eventos.publishEvent(EventoAuditoria.de(acao, "Solicitacao", solicitacao.getId(), detalhes));
    }
}
