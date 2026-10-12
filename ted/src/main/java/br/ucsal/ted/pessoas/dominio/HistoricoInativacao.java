package br.ucsal.ted.pessoas.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "historico_inativacao", schema = "pessoas")
public class HistoricoInativacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 20)
    private TipoPessoa tipoPessoa;

    @Column(name = "pessoa_id", nullable = false)
    private Long pessoaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MotivoInativacao motivo;

    @Column(length = 500)
    private String descricao;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @Column(name = "registrado_por", nullable = false, length = 100)
    private String registradoPor;

    protected HistoricoInativacao() {
    }

    HistoricoInativacao(TipoPessoa tipoPessoa, Long pessoaId, MotivoInativacao motivo, String descricao,
                        String registradoPor) {
        this.tipoPessoa = tipoPessoa;
        this.pessoaId = pessoaId;
        this.motivo = motivo;
        this.descricao = descricao;
        this.registradoPor = registradoPor;
        this.dataHora = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public Long getPessoaId() {
        return pessoaId;
    }

    public MotivoInativacao getMotivo() {
        return motivo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getDataHora() {
        return dataHora;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }
}
