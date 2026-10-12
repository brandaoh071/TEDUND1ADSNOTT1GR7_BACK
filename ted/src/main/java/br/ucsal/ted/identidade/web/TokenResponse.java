package br.ucsal.ted.identidade.web;

import br.ucsal.ted.identidade.aplicacao.TokenService;

import java.time.Instant;
import java.util.List;

public record TokenResponse(String token, String tipo, Instant expiraEm, String login, List<String> perfis) {

    public static TokenResponse de(TokenService.TokenGerado gerado) {
        return new TokenResponse(gerado.token(), "Bearer", gerado.expiraEm(), gerado.login(), gerado.perfis());
    }
}
