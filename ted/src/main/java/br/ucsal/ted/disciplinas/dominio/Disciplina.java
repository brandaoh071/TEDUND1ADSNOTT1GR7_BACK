package br.ucsal.ted.disciplinas.dominio;

import br.ucsal.ted.compartilhado.dominio.EntidadeCadastral;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "disciplina", schema = "disciplinas")
public class Disciplina extends EntidadeCadastral {

    @Column(nullable = false, length = 20)
    private String sigla;

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    @Column(name = "carga_horaria", nullable = false)
    private int cargaHoraria;

    @Column(name = "docente_id", nullable = false)
    private Long docenteId;

    protected Disciplina() {
    }

    public Disciplina(String sigla, String descricao, Long cursoId, int cargaHoraria, Long docenteId) {
        atualizar(sigla, descricao, cursoId, cargaHoraria, docenteId);
    }

    public void atualizar(String sigla, String descricao, Long cursoId, int cargaHoraria, Long docenteId) {
        if (cursoId == null) {
            throw new RegraNegocioException("A disciplina precisa estar vinculada a um curso");
        }
        if (docenteId == null) {
            throw new RegraNegocioException("A disciplina precisa ter um docente responsável");
        }
        if (cargaHoraria <= 0) {
            throw new RegraNegocioException("A carga horária precisa ser maior que zero");
        }
        this.sigla = sigla.trim().toUpperCase();
        this.descricao = descricao.trim();
        this.cursoId = cursoId;
        this.cargaHoraria = cargaHoraria;
        this.docenteId = docenteId;
    }

    public boolean lecionadaPor(Long docenteId) {
        return this.docenteId.equals(docenteId);
    }

    public String getSigla() {
        return sigla;
    }

    public String getDescricao() {
        return descricao;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public Long getDocenteId() {
        return docenteId;
    }
}
