package br.ucsal.ted.estrutura.aplicacao.dto;

import br.ucsal.ted.estrutura.dominio.Turno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CursoRequest(
        @NotBlank @Size(max = 20) String sigla,
        @NotBlank @Size(max = 150) String descricao,
        @NotBlank @Size(max = 150) String coordenador,
        @NotNull Long centroAcademicoId,
        @NotEmpty Set<Turno> turnos) {
}
