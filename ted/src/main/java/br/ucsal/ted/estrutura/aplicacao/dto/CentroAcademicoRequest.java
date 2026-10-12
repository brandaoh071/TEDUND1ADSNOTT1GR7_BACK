package br.ucsal.ted.estrutura.aplicacao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CentroAcademicoRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 150) String emailCentral,
        @NotBlank @Size(max = 20) String telefoneCentral,
        @NotBlank @Size(max = 150) String coordenador,
        @NotNull Long iesId) {
}
