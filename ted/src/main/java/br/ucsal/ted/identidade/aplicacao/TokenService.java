package br.ucsal.ted.identidade.aplicacao;

import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;
import br.ucsal.ted.identidade.dominio.Usuario;
import br.ucsal.ted.identidade.infra.JwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final JwtProperties propriedades;

    public TokenService(JwtEncoder encoder, JwtProperties propriedades) {
        this.encoder = encoder;
        this.propriedades = propriedades;
    }

    public TokenGerado gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(propriedades.expiracao());
        List<String> perfis = usuario.nomesDosPerfis().stream().map(Enum::name).sorted().toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.emissor())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getLogin())
                .claim(UsuarioLogado.CLAIM_USUARIO_ID, usuario.getId())
                .claim(UsuarioLogado.CLAIM_PERFIS, perfis)
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenGerado(token, expiraEm, usuario.getId(), usuario.getLogin(), perfis);
    }

    public record TokenGerado(String token, Instant expiraEm, Long usuarioId, String login, List<String> perfis) {
    }
}
