package br.ucsal.ted.compartilhado.dominio;

import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadeCadastral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private Instant dataCadastro;

    @CreatedBy
    @Column(name = "criado_por", updatable = false)
    private String criadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ATIVO;

    public void inativar() {
        if (!estaAtiva()) {
            throw new RegraNegocioException("O registro já está inativo");
        }
        status = Status.INATIVO;
    }

    public void reativar() {
        if (estaAtiva()) {
            throw new RegraNegocioException("O registro já está ativo");
        }
        status = Status.ATIVO;
    }

    public boolean estaAtiva() {
        return status == Status.ATIVO;
    }

    public Long getId() {
        return id;
    }

    public Instant getDataCadastro() {
        return dataCadastro;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public Status getStatus() {
        return status;
    }
}
