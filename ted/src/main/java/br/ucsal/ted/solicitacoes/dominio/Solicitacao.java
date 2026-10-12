package br.ucsal.ted.solicitacoes.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitacao", schema = "solicitacoes")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discente_id", nullable = false, updatable = false)
    private Long discenteId;

    @Column(name = "curso_id", nullable = false, updatable = false)
    private Long cursoId;

    @Column(name = "centro_academico_id", nullable = false, updatable = false)
    private Long centroAcademicoId;

    @Column(name = "semestre_referencia", nullable = false, length = 6, updatable = false)
    private String semestreReferencia;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private Instant dataSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSolicitacao status;

    @Column(name = "validado_por", length = 100)
    private String validadoPor;

    @Column(name = "data_validacao")
    private Instant dataValidacao;

    @Column(length = 500)
    private String observacao;

    @Version
    private long versao;

    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("prioridade DESC, id ASC")
    private List<ItemSolicitacao> itens = new ArrayList<>();

    protected Solicitacao() {
    }

    public Solicitacao(Long discenteId, Long cursoId, Long centroAcademicoId, String semestreReferencia,
                       List<ItemSolicitado> itensSolicitados) {
        this.discenteId = discenteId;
        this.cursoId = cursoId;
        this.centroAcademicoId = centroAcademicoId;
        this.semestreReferencia = semestreReferencia;
        this.dataSolicitacao = Instant.now();
        this.status = StatusSolicitacao.AGUARDANDO;
        itensSolicitados.forEach(i -> itens.add(new ItemSolicitacao(this, i.disciplinaId(), i.prioridade())));
    }

    public void aprovar(String responsavel) {
        status = status.aprovar();
        registrarValidacao(responsavel, null);
    }

    public void enviarParaListaDeEspera(String responsavel, String observacao) {
        status = status.enviarParaListaDeEspera();
        registrarValidacao(responsavel, observacao);
    }

    private void registrarValidacao(String responsavel, String observacao) {
        this.validadoPor = responsavel;
        this.dataValidacao = Instant.now();
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
    }

    public Long getId() {
        return id;
    }

    public Long getDiscenteId() {
        return discenteId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public Long getCentroAcademicoId() {
        return centroAcademicoId;
    }

    public String getSemestreReferencia() {
        return semestreReferencia;
    }

    public Instant getDataSolicitacao() {
        return dataSolicitacao;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public String getValidadoPor() {
        return validadoPor;
    }

    public Instant getDataValidacao() {
        return dataValidacao;
    }

    public String getObservacao() {
        return observacao;
    }

    public List<ItemSolicitacao> getItens() {
        return List.copyOf(itens);
    }
}
