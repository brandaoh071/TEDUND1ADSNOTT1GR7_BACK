package br.ucsal.ted.solicitacoes.dominio.regras;

import br.ucsal.ted.solicitacoes.dominio.ItemSolicitado;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(5)
public class DisciplinasDisponiveis implements RegraSolicitacao {

    @Override
    public Optional<String> verificar(ContextoSolicitacao contexto) {
        boolean indisponivel = contexto.itens().stream()
                .map(ItemSolicitado::disciplinaId)
                .anyMatch(id -> !contexto.disciplinasAtivas().getOrDefault(id, false));
        if (indisponivel) {
            return Optional.of("Só é possível escolher disciplinas ativas cadastradas pelos colaboradores");
        }
        return Optional.empty();
    }
}
