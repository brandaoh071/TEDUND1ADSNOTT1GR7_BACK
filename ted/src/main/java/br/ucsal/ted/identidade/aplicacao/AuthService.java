package br.ucsal.ted.identidade.aplicacao;

import br.ucsal.ted.identidade.infra.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarios;
    private final TokenService tokens;
    private final RegistroDeAcesso registro;

    public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarios,
                       TokenService tokens, RegistroDeAcesso registro) {
        this.authenticationManager = authenticationManager;
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.registro = registro;
    }

    public TokenService.TokenGerado autenticar(String login, String senha) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, senha));
        } catch (AuthenticationException ex) {
            registro.falha(login);
            throw new BadCredentialsException("Login ou senha inválidos", ex);
        }
        TokenService.TokenGerado gerado = usuarios.findByLoginIgnoreCase(login)
                .map(tokens::gerar)
                .orElseThrow(() -> new BadCredentialsException("Login ou senha inválidos"));
        registro.sucesso(gerado.usuarioId(), gerado.login(), String.join(",", gerado.perfis()));
        return gerado;
    }
}
