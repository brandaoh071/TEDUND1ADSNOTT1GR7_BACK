package br.ucsal.ted.solicitacoes.dominio.regras;

import br.ucsal.ted.solicitacoes.dominio.ItemSolicitado;

import java.util.List;
import java.util.Map;

public record ContextoSolicitacao(
        List<ItemSolicitado> itens,
        Map<Long, Boolean> disciplinasAtivas,
        boolean jaSolicitouNoSemestre,
        String semestreReferencia) {
}
