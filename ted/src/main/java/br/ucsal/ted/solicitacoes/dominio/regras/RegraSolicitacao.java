package br.ucsal.ted.solicitacoes.dominio.regras;

import java.util.Optional;

public interface RegraSolicitacao {

    Optional<String> verificar(ContextoSolicitacao contexto);
}
