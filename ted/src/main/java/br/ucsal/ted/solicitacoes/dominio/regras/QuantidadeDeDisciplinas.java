package br.ucsal.ted.solicitacoes.dominio.regras;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(1)
public class QuantidadeDeDisciplinas implements RegraSolicitacao {

    public static final int MINIMO = 2;
    public static final int MAXIMO = 9;

    @Override
    public Optional<String> verificar(ContextoSolicitacao contexto) {
        int quantidade = contexto.itens().size();
        if (quantidade < MINIMO || quantidade > MAXIMO) {
            return Optional.of("Escolha no mínimo " + MINIMO + " e no máximo " + MAXIMO
                    + " disciplinas (foram escolhidas " + quantidade + ")");
        }
        return Optional.empty();
    }
}
