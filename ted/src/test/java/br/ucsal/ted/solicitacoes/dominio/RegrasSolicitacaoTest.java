package br.ucsal.ted.solicitacoes.dominio;

import br.ucsal.ted.solicitacoes.dominio.regras.ContextoSolicitacao;
import br.ucsal.ted.solicitacoes.dominio.regras.DisciplinasDisponiveis;
import br.ucsal.ted.solicitacoes.dominio.regras.DisciplinasSemRepeticao;
import br.ucsal.ted.solicitacoes.dominio.regras.LimiteDePrioridadeMaxima;
import br.ucsal.ted.solicitacoes.dominio.regras.PrioridadeNoIntervalo;
import br.ucsal.ted.solicitacoes.dominio.regras.QuantidadeDeDisciplinas;
import br.ucsal.ted.solicitacoes.dominio.regras.UmaSolicitacaoPorSemestre;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class RegrasSolicitacaoTest {

    private ContextoSolicitacao contexto(List<ItemSolicitado> itens) {
        Map<Long, Boolean> ativas = itens.stream()
                .collect(Collectors.toMap(ItemSolicitado::disciplinaId, i -> true, (a, b) -> a));
        return new ContextoSolicitacao(itens, ativas, false, "2027.1");
    }

    private List<ItemSolicitado> itens(int quantidade, int prioridade) {
        return LongStream.rangeClosed(1, quantidade).mapToObj(id -> new ItemSolicitado(id, prioridade)).toList();
    }

    @Test
    void quantidadeEntreDoisENove() {
        QuantidadeDeDisciplinas regra = new QuantidadeDeDisciplinas();
        assertThat(regra.verificar(contexto(itens(1, 1)))).isPresent();
        assertThat(regra.verificar(contexto(itens(2, 1)))).isEmpty();
        assertThat(regra.verificar(contexto(itens(9, 1)))).isEmpty();
        assertThat(regra.verificar(contexto(itens(10, 1)))).isPresent();
    }

    @Test
    void prioridadeEntreUmECinco() {
        PrioridadeNoIntervalo regra = new PrioridadeNoIntervalo();
        assertThat(regra.verificar(contexto(itens(2, 0)))).isPresent();
        assertThat(regra.verificar(contexto(itens(2, 6)))).isPresent();
        assertThat(regra.verificar(contexto(List.of(new ItemSolicitado(1L, null))))).isPresent();
        assertThat(regra.verificar(contexto(itens(2, 4)))).isEmpty();
    }

    @Test
    void noMaximoDuasComPrioridadeMaxima() {
        LimiteDePrioridadeMaxima regra = new LimiteDePrioridadeMaxima();
        assertThat(regra.verificar(contexto(itens(2, 5)))).isEmpty();
        assertThat(regra.verificar(contexto(itens(3, 5)))).isPresent();
    }

    @Test
    void naoRepeteDisciplina() {
        DisciplinasSemRepeticao regra = new DisciplinasSemRepeticao();
        assertThat(regra.verificar(contexto(List.of(new ItemSolicitado(1L, 1), new ItemSolicitado(1L, 2)))))
                .isPresent();
    }

    @Test
    void soDisciplinasAtivas() {
        DisciplinasDisponiveis regra = new DisciplinasDisponiveis();
        ContextoSolicitacao inativa = new ContextoSolicitacao(itens(2, 1), Map.of(1L, true, 2L, false), false, "2027.1");
        ContextoSolicitacao inexistente = new ContextoSolicitacao(itens(2, 1), Map.of(1L, true), false, "2027.1");
        assertThat(regra.verificar(inativa)).isPresent();
        assertThat(regra.verificar(inexistente)).isPresent();
        assertThat(regra.verificar(contexto(itens(2, 1)))).isEmpty();
    }

    @Test
    void umaSolicitacaoPorSemestre() {
        UmaSolicitacaoPorSemestre regra = new UmaSolicitacaoPorSemestre();
        assertThat(regra.verificar(new ContextoSolicitacao(itens(2, 1), Map.of(), true, "2027.1"))).isPresent();
    }
}
