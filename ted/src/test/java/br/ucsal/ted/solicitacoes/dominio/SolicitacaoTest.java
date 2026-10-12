package br.ucsal.ted.solicitacoes.dominio;

import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolicitacaoTest {

    private Solicitacao nova() {
        return new Solicitacao(1L, 2L, 3L, "2027.1",
                List.of(new ItemSolicitado(10L, 5), new ItemSolicitado(11L, 3)));
    }

    @Test
    void nasceAguardandoComItensOrdenadosPelaPrioridade() {
        Solicitacao solicitacao = nova();
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.AGUARDANDO);
        assertThat(solicitacao.getItens()).hasSize(2);
        assertThat(solicitacao.getDataSolicitacao()).isNotNull();
    }

    @Test
    void aprovarLevaParaEmAnaliseERegistraQuemValidou() {
        Solicitacao solicitacao = nova();
        solicitacao.aprovar("doc1");
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.EM_ANALISE);
        assertThat(solicitacao.getValidadoPor()).isEqualTo("doc1");
        assertThat(solicitacao.getDataValidacao()).isNotNull();
    }

    @Test
    void listaDeEsperaPodeSerAprovadaDepois() {
        Solicitacao solicitacao = nova();
        solicitacao.enviarParaListaDeEspera("doc1", "Turma cheia");
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.LISTA_ESPERA);
        assertThat(solicitacao.getObservacao()).isEqualTo("Turma cheia");
        solicitacao.aprovar("doc1");
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacao.EM_ANALISE);
    }

    @Test
    void transicoesInvalidasSaoBloqueadas() {
        Solicitacao solicitacao = nova();
        solicitacao.aprovar("doc1");
        assertThatThrownBy(() -> solicitacao.aprovar("doc1")).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> solicitacao.enviarParaListaDeEspera("doc1", null))
                .isInstanceOf(RegraNegocioException.class);

        Solicitacao outra = nova();
        outra.enviarParaListaDeEspera("doc1", null);
        assertThatThrownBy(() -> outra.enviarParaListaDeEspera("doc1", null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void semestreSubsequente() {
        assertThat(SemestreReferencia.subsequente(LocalDate.of(2026, 3, 10))).isEqualTo("2026.2");
        assertThat(SemestreReferencia.subsequente(LocalDate.of(2026, 10, 12))).isEqualTo("2027.1");
    }
}
