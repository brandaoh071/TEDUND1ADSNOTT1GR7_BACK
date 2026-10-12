package br.ucsal.ted.disciplinas.aplicacao.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DisciplinaRequest(
        @NotBlank @Size(max = 20) String sigla,
        @NotBlank @Size(max = 150) String descricao,
        @NotNull Long cursoId,
        @NotNull @Min(1) @Max(1000) Integer cargaHoraria,
        Long docenteId) {
}
