package br.ucsal.ted.estrutura.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProReitoria {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150)
    private String nomeado;

    protected ProReitoria() {
    }

    public ProReitoria(String nome, String nomeado) {
        this.nome = nome;
        this.nomeado = nomeado;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeado() {
        return nomeado;
    }
}
