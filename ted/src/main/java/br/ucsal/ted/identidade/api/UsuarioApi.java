package br.ucsal.ted.identidade.api;

import java.util.Set;

public interface UsuarioApi {

    Long criarConta(String login, String senha, Set<NomePerfil> perfis);

    void desativarConta(Long usuarioId);

    void reativarConta(Long usuarioId);
}
