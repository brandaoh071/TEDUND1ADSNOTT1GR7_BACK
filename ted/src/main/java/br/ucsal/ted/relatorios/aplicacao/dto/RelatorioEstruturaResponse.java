package br.ucsal.ted.relatorios.aplicacao.dto;

import br.ucsal.ted.estrutura.api.IesFicha;

import java.util.List;

public record RelatorioEstruturaResponse(EmissaoResponse emissao, List<IesFicha> instituicoes) {
}
