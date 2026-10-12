package br.ucsal.ted.solicitacoes.dominio.regras;

import br.ucsal.ted.solicitacoes.dominio.ItemSolicitacao;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(3)
public class PrioridadeNoIntervalo implements RegraSolicitacao {

    @Override
    public Optional<String> verificar(ContextoSolicitacao contexto) {
        boolean foraDoIntervalo = contexto.itens().stream()
                .anyMatch(i -> i.prioridade() == null
                        || i.prioridade() < ItemSolicitacao.PRIORIDADE_MINIMA
                        || i.prioridade() > ItemSolicitacao.PRIORIDADE_MAXIMA);
        if (foraDoIntervalo) {
            return Optional.of("Toda disciplina precisa de prioridade entre " + ItemSolicitacao.PRIORIDADE_MINIMA
                    + " e " + ItemSolicitacao.PRIORIDADE_MAXIMA);
        }
        return Optional.empty();
    }
}
