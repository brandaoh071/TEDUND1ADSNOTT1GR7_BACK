package br.ucsal.ted.relatorios.aplicacao.dto;

import java.util.List;

public record RelatorioDiscenteResponse(
        EmissaoResponse emissao,
        PessoaDoRelatorio discente,
        List<SolicitacaoDoRelatorio> solicitacoes) {
}
