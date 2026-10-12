package br.ucsal.ted.identidade.aplicacao;

import br.ucsal.ted.identidade.infra.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public DbUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) {
        return usuarios.findByLoginIgnoreCase(login)
                .map(u -> User.withUsername(u.getLogin())
                        .password(u.getSenhaHash())
                        .disabled(!u.isAtivo())
                        .roles(u.nomesDosPerfis().stream().map(Enum::name).toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(login));
    }
}
