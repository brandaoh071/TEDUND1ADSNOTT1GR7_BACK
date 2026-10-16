package br.ucsal.ted.relatorios.dominio;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.identidade.api.NomePerfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import java.time.Instant;

@Entity
@Immutable
@Table(name = "relatorio", schema = "relatorios")
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoRelatorio tipo;

    @Column(name = "emitido_por", nullable = false, length = 100)
    private String emitidoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_emissor", nullable = false, length = 20)
    private NomePerfil perfilEmissor;

    @Column(name = "data_emissao", nullable = false)
    private Instant dataEmissao;

    @Enumerated(EnumType.STRING)
    @Column(name = "filtro_status", length = 20)
    private Status filtroStatus;

    protected Relatorio() {
    }

    public Relatorio(TipoRelatorio tipo, String emitidoPor, NomePerfil perfilEmissor, Status filtroStatus) {
        this.tipo = tipo;
        this.emitidoPor = emitidoPor;
        this.perfilEmissor = perfilEmissor;
        this.filtroStatus = filtroStatus;
        this.dataEmissao = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TipoRelatorio getTipo() {
        return tipo;
    }

    public String getEmitidoPor() {
        return emitidoPor;
    }

    public NomePerfil getPerfilEmissor() {
        return perfilEmissor;
    }

    public Instant getDataEmissao() {
        return dataEmissao;
    }

    public Status getFiltroStatus() {
        return filtroStatus;
    }
}
