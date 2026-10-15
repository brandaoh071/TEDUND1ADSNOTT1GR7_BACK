package br.ucsal.ted.relatorios.aplicacao.dto;

import java.util.List;

public record RelatorioColaboradorResponse(
        EmissaoResponse emissao,
        PessoaDoRelatorio docente,
        List<DisciplinaDoRelatorio> disciplinas,
        List<PessoaDoRelatorio> discentes,
        List<SolicitacaoDoRelatorio> solicitacoesValidadas) {
}
