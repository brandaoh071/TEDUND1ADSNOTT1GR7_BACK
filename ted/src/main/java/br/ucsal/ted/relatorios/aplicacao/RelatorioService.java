package br.ucsal.ted.relatorios.aplicacao;

import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.auditoria.EventoAuditoria;
import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;
import br.ucsal.ted.disciplinas.api.DisciplinaFicha;
import br.ucsal.ted.disciplinas.api.DisciplinaResumo;
import br.ucsal.ted.disciplinas.api.DisciplinasApi;
import br.ucsal.ted.estrutura.api.CentroAcademicoResumo;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.estrutura.api.EstruturaApi;
import br.ucsal.ted.identidade.api.NomePerfil;
import br.ucsal.ted.pessoas.api.DiscenteResumo;
import br.ucsal.ted.pessoas.api.PessoaFicha;
import br.ucsal.ted.pessoas.api.PessoasApi;
import br.ucsal.ted.relatorios.aplicacao.dto.DisciplinaDoRelatorio;
import br.ucsal.ted.relatorios.aplicacao.dto.DocentesDoCentro;
import br.ucsal.ted.relatorios.aplicacao.dto.EmissaoResponse;
import br.ucsal.ted.relatorios.aplicacao.dto.PessoaDoRelatorio;
import br.ucsal.ted.relatorios.aplicacao.dto.RelatorioColaboradorResponse;
import br.ucsal.ted.relatorios.aplicacao.dto.RelatorioDiscenteResponse;
import br.ucsal.ted.relatorios.aplicacao.dto.RelatorioDocentesResponse;
import br.ucsal.ted.relatorios.aplicacao.dto.RelatorioEstruturaResponse;
import br.ucsal.ted.relatorios.aplicacao.dto.SolicitacaoDoRelatorio;
import br.ucsal.ted.relatorios.dominio.Relatorio;
import br.ucsal.ted.relatorios.dominio.TipoRelatorio;
import br.ucsal.ted.relatorios.infra.RelatorioRepository;
import br.ucsal.ted.solicitacoes.api.SolicitacaoFicha;
import br.ucsal.ted.solicitacoes.api.SolicitacoesApi;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorios;
    private final EstruturaApi estrutura;
    private final PessoasApi pessoas;
    private final DisciplinasApi disciplinas;
    private final SolicitacoesApi solicitacoes;
    private final ApplicationEventPublisher eventos;

    public RelatorioService(RelatorioRepository relatorios, EstruturaApi estrutura, PessoasApi pessoas,
                            DisciplinasApi disciplinas, SolicitacoesApi solicitacoes,
                            ApplicationEventPublisher eventos) {
        this.relatorios = relatorios;
        this.estrutura = estrutura;
        this.pessoas = pessoas;
        this.disciplinas = disciplinas;
        this.solicitacoes = solicitacoes;
        this.eventos = eventos;
    }

    @Transactional
    public RelatorioEstruturaResponse emitirEstrutura() {
        EmissaoResponse emissao = registrar(TipoRelatorio.ESTRUTURA_ACADEMICA, NomePerfil.ADMIN, null, null);
        return new RelatorioEstruturaResponse(emissao, estrutura.listarEstruturaCompleta());
    }

    @Transactional
    public RelatorioDocentesResponse emitirDocentes(Status filtroStatus) {
        List<PessoaFicha> docentes = pessoas.listarFichasDeDocentes().stream()
                .filter(d -> filtroStatus == null || d.status() == filtroStatus)
                .toList();
        Map<Long, CentroAcademicoResumo> centros = estrutura.buscarCentrosAcademicos(
                ids(docentes, PessoaFicha::vinculoId));
        List<DocentesDoCentro> porCentro = docentes.stream()
                .collect(Collectors.groupingBy(PessoaFicha::vinculoId))
                .entrySet().stream()
                .map(grupo -> docentesDoCentro(centros.get(grupo.getKey()), grupo.getKey(), grupo.getValue()))
                .sorted(Comparator.comparing(DocentesDoCentro::centroAcademico))
                .toList();
        long ativos = docentes.stream().filter(d -> d.status() == Status.ATIVO).count();
        EmissaoResponse emissao = registrar(TipoRelatorio.DOCENTES, NomePerfil.ADMIN, filtroStatus, null);
        return new RelatorioDocentesResponse(emissao, porCentro, ativos, docentes.size() - ativos);
    }

    @Transactional
    public RelatorioColaboradorResponse emitirColaborador() {
        UsuarioLogado usuario = UsuarioLogado.atual();
        Optional<PessoaFicha> docente = pessoas.buscarFichaDoDocentePorUsuario(usuario.id());
        PessoaDoRelatorio identificacao = docente
                .map(d -> PessoaDoRelatorio.de(d, nomeDoCentro(estrutura.buscarCentroAcademico(d.vinculoId())
                        .orElse(null))))
                .orElse(null);

        List<DisciplinaFicha> fichasDeDisciplinas = disciplinas.listarFichasDoColaborador(
                docente.map(PessoaFicha::id).orElse(null), usuario.login());
        List<PessoaFicha> fichasDeDiscentes = pessoas.listarFichasDeDiscentesCadastradosPor(usuario.login());
        Map<Long, CursoResumo> cursos = estrutura.buscarCursos(Stream.concat(
                        fichasDeDisciplinas.stream().map(DisciplinaFicha::cursoId),
                        fichasDeDiscentes.stream().map(PessoaFicha::vinculoId))
                .collect(Collectors.toSet()));

        List<DisciplinaDoRelatorio> disciplinasDoColaborador = fichasDeDisciplinas.stream()
                .map(d -> new DisciplinaDoRelatorio(d.id(), d.sigla(), d.descricao(),
                        nomeDoCurso(cursos.get(d.cursoId())), d.cargaHoraria(), d.dataCadastro(), d.status()))
                .toList();
        List<PessoaDoRelatorio> discentes = fichasDeDiscentes.stream()
                .map(d -> PessoaDoRelatorio.de(d, nomeDoCurso(cursos.get(d.vinculoId()))))
                .toList();
        List<SolicitacaoDoRelatorio> validadas = montarSolicitacoes(solicitacoes.listarValidadasPor(usuario.login()));

        NomePerfil perfil = usuario.possuiPerfil(NomePerfil.COLABORADOR.name())
                ? NomePerfil.COLABORADOR
                : NomePerfil.ADMIN;
        EmissaoResponse emissao = registrar(TipoRelatorio.COLABORADOR_COMPLETO, perfil, null,
                docente.map(PessoaFicha::nomeCompleto).orElse(null));
        return new RelatorioColaboradorResponse(emissao, identificacao, disciplinasDoColaborador, discentes,
                validadas);
    }

    @Transactional
    public RelatorioDiscenteResponse emitirDiscente() {
        PessoaFicha discente = pessoas.buscarFichaDoDiscentePorUsuario(UsuarioLogado.atual().id())
                .orElseThrow(() -> new AccessDeniedException("Somente discentes emitem este relatório"));
        String curso = nomeDoCurso(estrutura.buscarCurso(discente.vinculoId()).orElse(null));
        List<SolicitacaoDoRelatorio> lista = montarSolicitacoes(solicitacoes.listarDoDiscente(discente.id()));
        EmissaoResponse emissao = registrar(TipoRelatorio.DISCENTE_COMPLETO, NomePerfil.USUARIO, null,
                discente.nomeCompleto());
        return new RelatorioDiscenteResponse(emissao, PessoaDoRelatorio.de(discente, curso), lista);
    }

    private EmissaoResponse registrar(TipoRelatorio tipo, NomePerfil perfil, Status filtroStatus,
                                      String nomeEmissor) {
        UsuarioLogado usuario = UsuarioLogado.atual();
        Relatorio relatorio = relatorios.save(new Relatorio(tipo, usuario.login(), perfil, filtroStatus));
        eventos.publishEvent(EventoAuditoria.de(AcaoAuditada.EMITIR_RELATORIO, "Relatorio", relatorio.getId(),
                tipo.getTitulo() + (filtroStatus == null ? "" : " (somente " + filtroStatus + ")")));
        return EmissaoResponse.de(relatorio, nomeEmissor == null ? usuario.login() : nomeEmissor);
    }

    private DocentesDoCentro docentesDoCentro(CentroAcademicoResumo centro, Long centroId,
                                              List<PessoaFicha> docentes) {
        String nome = nomeDoCentro(centro);
        Map<Boolean, List<PessoaDoRelatorio>> separados = docentes.stream()
                .collect(Collectors.partitioningBy(d -> d.status() == Status.ATIVO,
                        Collectors.mapping(d -> PessoaDoRelatorio.de(d, nome), Collectors.toList())));
        return new DocentesDoCentro(centroId, centro == null ? "-" : centro.nome(),
                centro == null ? null : centro.iesSigla(), separados.get(true), separados.get(false));
    }

    private List<SolicitacaoDoRelatorio> montarSolicitacoes(List<SolicitacaoFicha> fichas) {
        Map<Long, DiscenteResumo> discentes = pessoas.buscarDiscentes(ids(fichas, SolicitacaoFicha::discenteId));
        Map<Long, DisciplinaResumo> mapaDisciplinas = disciplinas.buscarTodas(fichas.stream()
                .flatMap(s -> s.itens().stream())
                .map(SolicitacaoFicha.ItemFicha::disciplinaId)
                .collect(Collectors.toSet()));
        return fichas.stream()
                .map(s -> solicitacao(s, discentes.get(s.discenteId()), mapaDisciplinas))
                .toList();
    }

    private SolicitacaoDoRelatorio solicitacao(SolicitacaoFicha s, DiscenteResumo discente,
                                               Map<Long, DisciplinaResumo> mapaDisciplinas) {
        List<SolicitacaoDoRelatorio.ItemDoRelatorio> itens = s.itens().stream()
                .map(i -> {
                    DisciplinaResumo d = mapaDisciplinas.get(i.disciplinaId());
                    return new SolicitacaoDoRelatorio.ItemDoRelatorio(d == null ? null : d.sigla(),
                            d == null ? null : d.descricao(), d == null ? null : d.cargaHoraria(), i.prioridade());
                })
                .toList();
        return new SolicitacaoDoRelatorio(s.id(), discente == null ? null : discente.matricula(),
                discente == null ? null : discente.nomeCompleto(), s.semestreReferencia(), s.dataSolicitacao(),
                s.status(), s.validadoPor(), s.dataValidacao(), s.observacao(), itens);
    }

    private String nomeDoCentro(CentroAcademicoResumo centro) {
        return centro == null ? "-" : centro.nome() + " (" + centro.iesSigla() + ")";
    }

    private String nomeDoCurso(CursoResumo curso) {
        return curso == null ? "-" : curso.sigla() + " - " + curso.descricao();
    }

    private <T> Collection<Long> ids(Collection<T> itens, Function<T, Long> campo) {
        return itens.stream().map(campo).collect(Collectors.toSet());
    }
}
