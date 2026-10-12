package br.ucsal.ted.identidade.web;

import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;
import br.ucsal.ted.identidade.aplicacao.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica o usuário e devolve um JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.de(authService.autenticar(request.login(), request.senha()));
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado")
    public UsuarioLogadoResponse me() {
        return UsuarioLogadoResponse.de(UsuarioLogado.atual());
    }
}
