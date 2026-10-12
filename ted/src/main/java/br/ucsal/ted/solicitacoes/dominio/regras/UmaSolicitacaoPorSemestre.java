package br.ucsal.ted.solicitacoes.dominio.regras;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(6)
public class UmaSolicitacaoPorSemestre implements RegraSolicitacao {

    @Override
    public Optional<String> verificar(ContextoSolicitacao contexto) {
        if (contexto.jaSolicitouNoSemestre()) {
            return Optional.of("Você já enviou uma solicitação para o semestre " + contexto.semestreReferencia());
        }
        return Optional.empty();
    }
}
