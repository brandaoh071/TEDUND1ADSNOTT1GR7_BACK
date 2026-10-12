package br.ucsal.ted.estrutura.dominio;

import br.ucsal.ted.compartilhado.dominio.EntidadeCadastral;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "curso", schema = "estrutura")
public class Curso extends EntidadeCadastral {

    @Column(nullable = false, length = 20)
    private String sigla;

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(nullable = false, length = 150)
    private String coordenador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centro_academico_id", nullable = false)
    private CentroAcademico centroAcademico;

    @ElementCollection
    @CollectionTable(name = "curso_turno", schema = "estrutura", joinColumns = @JoinColumn(name = "curso_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "turno", nullable = false, length = 20)
    private Set<Turno> turnos = new HashSet<>();

    protected Curso() {
    }

    public Curso(String sigla, String descricao, String coordenador, CentroAcademico centroAcademico,
                 Set<Turno> turnos) {
        atualizar(sigla, descricao, coordenador, centroAcademico, turnos);
    }

    public void atualizar(String sigla, String descricao, String coordenador, CentroAcademico centroAcademico,
                          Set<Turno> turnos) {
        if (centroAcademico == null) {
            throw new RegraNegocioException("O curso precisa estar vinculado a um centro acadêmico");
        }
        if (turnos == null || turnos.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um turno oferecido");
        }
        this.sigla = sigla.trim().toUpperCase();
        this.descricao = descricao.trim();
        this.coordenador = coordenador.trim();
        this.centroAcademico = centroAcademico;
        this.turnos.clear();
        this.turnos.addAll(turnos);
    }

    public String getSigla() {
        return sigla;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCoordenador() {
        return coordenador;
    }

    public CentroAcademico getCentroAcademico() {
        return centroAcademico;
    }

    public Set<Turno> getTurnos() {
        return turnos.isEmpty() ? Set.of() : EnumSet.copyOf(turnos);
    }
}
