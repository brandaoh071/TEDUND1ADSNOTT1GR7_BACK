package br.ucsal.ted.identidade.aplicacao;

import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.identidade.api.NomePerfil;
import br.ucsal.ted.identidade.api.UsuarioApi;
import br.ucsal.ted.identidade.dominio.Perfil;
import br.ucsal.ted.identidade.dominio.Usuario;
import br.ucsal.ted.identidade.infra.PerfilRepository;
import br.ucsal.ted.identidade.infra.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UsuarioService implements UsuarioApi {

    private final UsuarioRepository usuarios;
    private final PerfilRepository perfis;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PerfilRepository perfis, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.perfis = perfis;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Long criarConta(String login, String senha, Set<NomePerfil> nomesPerfis) {
        if (usuarios.existsByLoginIgnoreCase(login)) {
            throw new RegraNegocioException("Já existe uma conta de acesso com o login " + login);
        }
        Set<Perfil> encontrados = perfis.findByNomeIn(nomesPerfis);
        Usuario usuario = new Usuario(login, passwordEncoder.encode(senha), encontrados);
        return usuarios.save(usuario).getId();
    }

    @Override
    @Transactional
    public void desativarConta(Long usuarioId) {
        buscar(usuarioId).desativar();
    }

    @Override
    @Transactional
    public void reativarConta(Long usuarioId) {
        buscar(usuarioId).reativar();
    }

    private Usuario buscar(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", usuarioId));
    }
}
