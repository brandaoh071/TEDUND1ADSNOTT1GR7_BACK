package br.ucsal.ted.compartilhado.seguranca;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record UsuarioLogado(Long id, String login, Set<String> perfis) {

    public static final String CLAIM_USUARIO_ID = "uid";
    public static final String CLAIM_PERFIS = "perfis";
    private static final UsuarioLogado ANONIMO = new UsuarioLogado(null, "anonimo", Set.of());

    public static UsuarioLogado atual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof Jwt jwt)) {
            return ANONIMO;
        }
        Number id = jwt.getClaim(CLAIM_USUARIO_ID);
        Collection<String> perfis = jwt.getClaimAsStringList(CLAIM_PERFIS);
        return new UsuarioLogado(
                id == null ? null : id.longValue(),
                jwt.getSubject(),
                perfis == null ? Set.of() : Set.copyOf(perfis));
    }

    public boolean possuiPerfil(String perfil) {
        return perfis.contains(perfil);
    }

    public boolean ehAdministrador() {
        return possuiPerfil("ADMIN");
    }

    public String perfisComoTexto() {
        return perfis.stream().sorted().collect(Collectors.joining(","));
    }

    public List<String> perfisOrdenados() {
        return perfis.stream().sorted().toList();
    }
}
