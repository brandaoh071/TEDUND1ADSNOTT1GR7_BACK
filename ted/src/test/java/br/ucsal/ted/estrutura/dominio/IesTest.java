package br.ucsal.ted.estrutura.dominio;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IesTest {

    private Ies novaIes() {
        return new Ies(" ucsal ", "Universidade Católica do Salvador", "a@b.com", "71", "Reitor",
                List.of(new ProReitoria("Graduação", "Fulano")));
    }

    @Test
    void normalizaSiglaENasceAtiva() {
        Ies ies = novaIes();
        assertThat(ies.getSigla()).isEqualTo("UCSAL");
        assertThat(ies.getStatus()).isEqualTo(Status.ATIVO);
    }

    @Test
    void naoAceitaProReitoriaRepetida() {
        assertThatThrownBy(() -> new Ies("X", "X", "a@b.com", "1", "R",
                List.of(new ProReitoria("Graduação", "A"), new ProReitoria("graduação", "B"))))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void inativaEReativaSemExcluir() {
        Ies ies = novaIes();
        ies.inativar();
        assertThat(ies.estaAtiva()).isFalse();
        assertThatThrownBy(ies::inativar).isInstanceOf(RegraNegocioException.class);
        ies.reativar();
        assertThat(ies.estaAtiva()).isTrue();
    }

    @Test
    void cursoExigeCentroAcademicoETurno() {
        CentroAcademico ca = new CentroAcademico("CCET", "a@b.com", "1", "Coord", novaIes());
        assertThatThrownBy(() -> new Curso("ES", "Eng", "C", null, Set.of(Turno.NOTURNO)))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> new Curso("ES", "Eng", "C", ca, Set.of()))
                .isInstanceOf(RegraNegocioException.class);
    }
}
