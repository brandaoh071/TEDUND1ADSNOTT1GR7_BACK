package br.ucsal.ted.auditoria.dominio;

import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.auditoria.EventoAuditoria;
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
@Table(name = "log_auditoria", schema = "auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(length = 100)
    private String perfis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AcaoAuditada acao;

    @Column(nullable = false, length = 60)
    private String entidade;

    @Column(name = "id_registro")
    private Long idRegistro;

    @Column(length = 1000)
    private String detalhes;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected LogAuditoria() {
    }

    public static LogAuditoria de(EventoAuditoria evento) {
        LogAuditoria log = new LogAuditoria();
        log.usuario = evento.usuario();
        log.perfis = evento.perfis();
        log.acao = evento.acao();
        log.entidade = evento.entidade();
        log.idRegistro = evento.idRegistro();
        log.detalhes = evento.detalhes() == null ? null
                : evento.detalhes().substring(0, Math.min(evento.detalhes().length(), 1000));
        log.dataHora = evento.dataHora();
        return log;
    }

    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPerfis() {
        return perfis;
    }

    public AcaoAuditada getAcao() {
        return acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public Long getIdRegistro() {
        return idRegistro;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public Instant getDataHora() {
        return dataHora;
    }
}
