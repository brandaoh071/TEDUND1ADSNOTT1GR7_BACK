package br.ucsal.ted.relatorios.aplicacao.dto;

import java.util.List;

public record DocentesDoCentro(
        Long centroAcademicoId,
        String centroAcademico,
        String iesSigla,
        List<PessoaDoRelatorio> ativos,
        List<PessoaDoRelatorio> inativos) {
}
