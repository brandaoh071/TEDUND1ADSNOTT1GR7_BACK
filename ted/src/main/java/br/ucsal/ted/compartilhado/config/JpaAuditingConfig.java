package br.ucsal.ted.compartilhado.config;

import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAtual")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAtual() {
        return () -> Optional.of(UsuarioLogado.atual().login());
    }
}
