package br.ucsal.ted.solicitacoes.api;

import java.util.List;

public interface SolicitacoesApi {

    List<SolicitacaoFicha> listarDoDiscente(Long discenteId);

    List<SolicitacaoFicha> listarValidadasPor(String login);
}
