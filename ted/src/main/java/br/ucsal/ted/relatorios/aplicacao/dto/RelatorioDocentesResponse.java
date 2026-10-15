package br.ucsal.ted.relatorios.aplicacao.dto;

import java.util.List;

public record RelatorioDocentesResponse(
        EmissaoResponse emissao,
        List<DocentesDoCentro> centrosAcademicos,
        long totalAtivos,
        long totalInativos) {
}
