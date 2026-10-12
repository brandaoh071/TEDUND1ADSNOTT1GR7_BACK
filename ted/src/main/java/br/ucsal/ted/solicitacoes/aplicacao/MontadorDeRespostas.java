package br.ucsal.ted.solicitacoes.aplicacao;

import br.ucsal.ted.disciplinas.api.DisciplinaResumo;
import br.ucsal.ted.disciplinas.api.DisciplinasApi;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.estrutura.api.EstruturaApi;
import br.ucsal.ted.pessoas.api.DiscenteResumo;
import br.ucsal.ted.pessoas.api.PessoasApi;
import br.ucsal.ted.solicitacoes.aplicacao.dto.ItemSolicitacaoResponse;
import br.ucsal.ted.solicitacoes.aplicacao.dto.SolicitacaoResponse;
import br.ucsal.ted.solicitacoes.dominio.ItemSolicitacao;
import br.ucsal.ted.solicitacoes.dominio.Solicitacao;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class MontadorDeRespostas {

    private final PessoasApi pessoas;
    private final DisciplinasApi disciplinas;
    private final EstruturaApi estrutura;

    MontadorDeRespostas(PessoasApi pessoas, DisciplinasApi disciplinas, EstruturaApi estrutura) {
        this.pessoas = pessoas;
        this.disciplinas = disciplinas;
        this.estrutura = estrutura;
    }

    SolicitacaoResponse montar(Solicitacao solicitacao, boolean proximaDaFila) {
        return montarTodas(List.of(solicitacao), proximaDaFila ? Set.of(solicitacao.getId()) : Set.of()).getFirst();
    }

    List<SolicitacaoResponse> montarTodas(Collection<Solicitacao> solicitacoes, Set<Long> proximasDaFila) {
        Map<Long, DiscenteResumo> discentes = pessoas.buscarDiscentes(
                ids(solicitacoes, Solicitacao::getDiscenteId));
        Map<Long, CursoResumo> cursos = estrutura.buscarCursos(ids(solicitacoes, Solicitacao::getCursoId));
        Map<Long, DisciplinaResumo> mapaDisciplinas = disciplinas.buscarTodas(solicitacoes.stream()
                .flatMap(s -> s.getItens().stream())
                .map(ItemSolicitacao::getDisciplinaId)
                .collect(Collectors.toSet()));
        return solicitacoes.stream()
                .map(s -> resposta(s, discentes.get(s.getDiscenteId()), cursos.get(s.getCursoId()), mapaDisciplinas,
                        proximasDaFila.contains(s.getId())))
                .toList();
    }

    private SolicitacaoResponse resposta(Solicitacao s, DiscenteResumo discente, CursoResumo curso,
                                         Map<Long, DisciplinaResumo> mapaDisciplinas, boolean proximaDaFila) {
        List<ItemSolicitacaoResponse> itens = s.getItens().stream()
                .map(i -> item(i, mapaDisciplinas.get(i.getDisciplinaId())))
                .toList();
        return new SolicitacaoResponse(s.getId(), s.getDiscenteId(),
                discente == null ? null : discente.matricula(),
                discente == null ? null : discente.nomeCompleto(),
                s.getCursoId(), curso == null ? null : curso.sigla(),
                s.getSemestreReferencia(), s.getDataSolicitacao(), s.getStatus(), itens,
                s.getValidadoPor(), s.getDataValidacao(), s.getObservacao(), proximaDaFila);
    }

    private ItemSolicitacaoResponse item(ItemSolicitacao item, DisciplinaResumo disciplina) {
        return new ItemSolicitacaoResponse(item.getDisciplinaId(),
                disciplina == null ? null : disciplina.sigla(),
                disciplina == null ? null : disciplina.descricao(),
                disciplina == null ? null : disciplina.cargaHoraria(),
                item.getPrioridade());
    }

    private Collection<Long> ids(Collection<Solicitacao> solicitacoes, Function<Solicitacao, Long> campo) {
        return solicitacoes.stream().map(campo).collect(Collectors.toSet());
    }
}
