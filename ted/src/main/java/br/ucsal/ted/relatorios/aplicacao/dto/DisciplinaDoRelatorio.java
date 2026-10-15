package br.ucsal.ted.relatorios.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;

import java.time.Instant;

public record DisciplinaDoRelatorio(
        Long id,
        String sigla,
        String descricao,
        String curso,
        int cargaHoraria,
        Instant dataCadastro,
        Status status) {
}
