package br.ucsal.ted.solicitacoes.dominio.regras;

import br.ucsal.ted.solicitacoes.dominio.ItemSolicitacao;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(4)
public class LimiteDePrioridadeMaxima implements RegraSolicitacao {

    public static final int LIMITE = 2;

    @Override
    public Optional<String> verificar(ContextoSolicitacao contexto) {
        long maximas = contexto.itens().stream()
                .filter(i -> i.prioridade() != null && i.prioridade() == ItemSolicitacao.PRIORIDADE_MAXIMA)
                .count();
        if (maximas > LIMITE) {
            return Optional.of("Apenas " + LIMITE + " disciplinas podem ter prioridade máxima ("
                    + ItemSolicitacao.PRIORIDADE_MAXIMA + "); foram marcadas " + maximas);
        }
        return Optional.empty();
    }
}
