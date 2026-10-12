package br.ucsal.ted.estrutura.dominio;

import br.ucsal.ted.compartilhado.dominio.EntidadeCadastral;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ies", schema = "estrutura")
public class Ies extends EntidadeCadastral {

    @Column(nullable = false, unique = true, length = 20)
    private String sigla;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "email_central", nullable = false, length = 150)
    private String emailCentral;

    @Column(name = "telefone_central", nullable = false, length = 20)
    private String telefoneCentral;

    @Column(nullable = false, length = 150)
    private String reitor;

    @ElementCollection
    @CollectionTable(name = "pro_reitoria", schema = "estrutura", joinColumns = @JoinColumn(name = "ies_id"))
    @OrderColumn(name = "ordem")
    private List<ProReitoria> proReitorias = new ArrayList<>();

    protected Ies() {
    }

    public Ies(String sigla, String nome, String emailCentral, String telefoneCentral, String reitor,
               List<ProReitoria> proReitorias) {
        atualizar(sigla, nome, emailCentral, telefoneCentral, reitor, proReitorias);
    }

    public void atualizar(String sigla, String nome, String emailCentral, String telefoneCentral, String reitor,
                          List<ProReitoria> proReitorias) {
        this.sigla = sigla.trim().toUpperCase();
        this.nome = nome.trim();
        this.emailCentral = emailCentral.trim();
        this.telefoneCentral = telefoneCentral.trim();
        this.reitor = reitor.trim();
        substituirProReitorias(proReitorias);
    }

    public void adicionarProReitoria(ProReitoria proReitoria) {
        boolean repetida = proReitorias.stream()
                .anyMatch(p -> p.getNome().equalsIgnoreCase(proReitoria.getNome()));
        if (repetida) {
            throw new RegraNegocioException("A pró-reitoria " + proReitoria.getNome() + " foi informada mais de uma vez");
        }
        proReitorias.add(proReitoria);
    }

    private void substituirProReitorias(List<ProReitoria> novas) {
        proReitorias.clear();
        if (novas != null) {
            novas.forEach(this::adicionarProReitoria);
        }
    }

    public String getSigla() {
        return sigla;
    }

    public String getNome() {
        return nome;
    }

    public String getEmailCentral() {
        return emailCentral;
    }

    public String getTelefoneCentral() {
        return telefoneCentral;
    }

    public String getReitor() {
        return reitor;
    }

    public List<ProReitoria> getProReitorias() {
        return List.copyOf(proReitorias);
    }
}
