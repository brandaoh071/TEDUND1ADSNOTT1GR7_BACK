package br.ucsal.ted.estrutura.aplicacao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record IesRequest(
        @NotBlank @Size(max = 20) String sigla,
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 150) String emailCentral,
        @NotBlank @Size(max = 20) String telefoneCentral,
        @NotBlank @Size(max = 150) String reitor,
        @Valid List<ProReitoriaDto> proReitorias) {
}
