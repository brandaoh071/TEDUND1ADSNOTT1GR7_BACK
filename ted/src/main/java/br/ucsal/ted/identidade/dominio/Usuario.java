package br.ucsal.ted.identidade.dominio;

import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.identidade.api.NomePerfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario", schema = "identidade")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_perfil", schema = "identidade",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private Set<Perfil> perfis = new HashSet<>();

    protected Usuario() {
    }

    public Usuario(String login, String senhaHash, Set<Perfil> perfis) {
        if (perfis == null || perfis.isEmpty()) {
            throw new RegraNegocioException("O usuário precisa ter ao menos um perfil");
        }
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfis.addAll(perfis);
    }

    public boolean temPerfil(NomePerfil nome) {
        return perfis.stream().anyMatch(p -> p.getNome() == nome);
    }

    public void adicionarPerfil(Perfil perfil) {
        perfis.add(perfil);
    }

    public void desativar() {
        ativo = false;
    }

    public void reativar() {
        ativo = true;
    }

    public Set<NomePerfil> nomesDosPerfis() {
        Set<NomePerfil> nomes = new HashSet<>();
        perfis.forEach(p -> nomes.add(p.getNome()));
        return nomes;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
