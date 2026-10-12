package br.ucsal.ted.disciplinas.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.disciplinas.dominio.Disciplina;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.pessoas.api.DocenteResumo;

import java.time.Instant;

public record DisciplinaResponse(
        Long id,
        String sigla,
        String descricao,
        Long cursoId,
        String cursoSigla,
        String cursoDescricao,
        int cargaHoraria,
        Long docenteId,
        String docenteNome,
        Instant dataCadastro,
        Status status) {

    public static DisciplinaResponse de(Disciplina disciplina, CursoResumo curso, DocenteResumo docente) {
        return new DisciplinaResponse(disciplina.getId(), disciplina.getSigla(), disciplina.getDescricao(),
                disciplina.getCursoId(), curso == null ? null : curso.sigla(),
                curso == null ? null : curso.descricao(), disciplina.getCargaHoraria(), disciplina.getDocenteId(),
                docente == null ? null : docente.nomeCompleto(), disciplina.getDataCadastro(),
                disciplina.getStatus());
    }
}
