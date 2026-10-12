package br.ucsal.ted.pessoas.dominio;

import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PessoaTest {

    @Test
    void inativarDocenteGeraHistoricoComMotivo() {
        Docente docente = new Docente("D1", "Ana", "ANA@X.COM", "71", 1L, 10L);
        HistoricoInativacao historico = docente.inativar(MotivoInativacao.AFASTAMENTO, "Licença", "admin");
        assertThat(docente.estaAtiva()).isFalse();
        assertThat(docente.getEmail()).isEqualTo("ana@x.com");
        assertThat(historico.getMotivo()).isEqualTo(MotivoInativacao.AFASTAMENTO);
        assertThat(historico.getTipoPessoa()).isEqualTo(TipoPessoa.DOCENTE);
        assertThat(historico.getRegistradoPor()).isEqualTo("admin");
    }

    @Test
    void docenteNaoPodeSerInativadoPorDesistencia() {
        Docente docente = new Docente("D1", "Ana", "ana@x.com", "71", 1L, 10L);
        assertThatThrownBy(() -> docente.inativar(MotivoInativacao.DESISTENCIA, null, "admin"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void discenteNaoPodeSerInativadoPorSuspensaoContratual() {
        Discente discente = new Discente("A1", "Bia", "bia@x.com", "71", 1L, 11L);
        assertThatThrownBy(() -> discente.inativar(MotivoInativacao.SUSPENSAO_CONTRATUAL, null, "col"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void motivoOutroExigeDescricao() {
        Discente discente = new Discente("A1", "Bia", "bia@x.com", "71", 1L, 11L);
        assertThatThrownBy(() -> discente.inativar(MotivoInativacao.OUTRO, " ", "col"))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void inativarSemMotivoNaoEPermitido() {
        Discente discente = new Discente("A1", "Bia", "bia@x.com", "71", 1L, 11L);
        assertThatThrownBy(discente::inativar).isInstanceOf(RegraNegocioException.class);
    }
}
