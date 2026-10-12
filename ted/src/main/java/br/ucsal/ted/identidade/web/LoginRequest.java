package br.ucsal.ted.identidade.web;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String login, @NotBlank String senha) {
}
