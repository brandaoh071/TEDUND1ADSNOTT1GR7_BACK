package br.ucsal.ted.estrutura.dominio;

import br.ucsal.ted.compartilhado.dominio.EntidadeCadastral;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "centro_academico", schema = "estrutura")
public class CentroAcademico extends EntidadeCadastral {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "email_central", nullable = false, length = 150)
    private String emailCentral;

    @Column(name = "telefone_central", nullable = false, length = 20)
    private String telefoneCentral;

    @Column(nullable = false, length = 150)
    private String coordenador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ies_id", nullable = false)
    private Ies ies;

    protected CentroAcademico() {
    }

    public CentroAcademico(String nome, String emailCentral, String telefoneCentral, String coordenador, Ies ies) {
        atualizar(nome, emailCentral, telefoneCentral, coordenador, ies);
    }

    public void atualizar(String nome, String emailCentral, String telefoneCentral, String coordenador, Ies ies) {
        if (ies == null) {
            throw new RegraNegocioException("O centro acadêmico precisa estar vinculado a uma IES");
        }
        this.nome = nome.trim();
        this.emailCentral = emailCentral.trim();
        this.telefoneCentral = telefoneCentral.trim();
        this.coordenador = coordenador.trim();
        this.ies = ies;
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

    public String getCoordenador() {
        return coordenador;
    }

    public Ies getIes() {
        return ies;
    }
}
