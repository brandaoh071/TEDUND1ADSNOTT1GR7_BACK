package br.ucsal.ted.identidade.dominio;

import br.ucsal.ted.identidade.api.NomePerfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "perfil", schema = "identidade")
public class Perfil {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private NomePerfil nome;

    protected Perfil() {
    }

    public Long getId() {
        return id;
    }

    public NomePerfil getNome() {
        return nome;
    }
}
