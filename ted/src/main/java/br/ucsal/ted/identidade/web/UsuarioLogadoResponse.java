package br.ucsal.ted.identidade.web;

import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;

import java.util.List;

public record UsuarioLogadoResponse(Long id, String login, List<String> perfis) {

    public static UsuarioLogadoResponse de(UsuarioLogado usuario) {
        return new UsuarioLogadoResponse(usuario.id(), usuario.login(), usuario.perfisOrdenados());
    }
}
