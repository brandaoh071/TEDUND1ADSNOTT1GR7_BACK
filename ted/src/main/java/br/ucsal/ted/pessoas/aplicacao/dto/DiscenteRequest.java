package br.ucsal.ted.pessoas.aplicacao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DiscenteRequest(
        @NotBlank @Size(max = 30) String matricula,
        @NotBlank @Size(max = 150) String nomeCompleto,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 20) String telefone,
        @NotNull Long cursoId,
        @Size(min = 6, max = 72) String senha) {
}
