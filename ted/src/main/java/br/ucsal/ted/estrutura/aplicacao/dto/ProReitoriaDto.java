package br.ucsal.ted.estrutura.aplicacao.dto;

import br.ucsal.ted.estrutura.dominio.ProReitoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProReitoriaDto(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Size(max = 150) String nomeado) {

    public static ProReitoriaDto de(ProReitoria proReitoria) {
        return new ProReitoriaDto(proReitoria.getNome(), proReitoria.getNomeado());
    }

    public ProReitoria paraDominio() {
        return new ProReitoria(nome.trim(), nomeado.trim());
    }
}
