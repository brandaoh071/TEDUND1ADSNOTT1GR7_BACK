package br.ucsal.ted.solicitacoes.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_solicitacao", schema = "solicitacoes")
public class ItemSolicitacao {

    public static final int PRIORIDADE_MINIMA = 1;
    public static final int PRIORIDADE_MAXIMA = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Column(name = "disciplina_id", nullable = false)
    private Long disciplinaId;

    @Column(nullable = false)
    private int prioridade;

    protected ItemSolicitacao() {
    }

    ItemSolicitacao(Solicitacao solicitacao, Long disciplinaId, int prioridade) {
        this.solicitacao = solicitacao;
        this.disciplinaId = disciplinaId;
        this.prioridade = prioridade;
    }

    public boolean ehPrioridadeMaxima() {
        return prioridade == PRIORIDADE_MAXIMA;
    }

    public Long getId() {
        return id;
    }

    public Long getDisciplinaId() {
        return disciplinaId;
    }

    public int getPrioridade() {
        return prioridade;
    }
}
