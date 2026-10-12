package br.ucsal.ted.solicitacoes.dominio.regras;

import br.ucsal.ted.solicitacoes.dominio.ItemSolicitado;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(2)
public class DisciplinasSemRepeticao implements RegraSolicitacao {

    @Override
    public Optional<String> verificar(ContextoSolicitacao contexto) {
        long distintas = contexto.itens().stream().map(ItemSolicitado::disciplinaId).distinct().count();
        if (distintas != contexto.itens().size()) {
            return Optional.of("A mesma disciplina foi escolhida mais de uma vez");
        }
        return Optional.empty();
    }
}
