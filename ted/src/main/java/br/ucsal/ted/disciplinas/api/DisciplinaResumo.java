package br.ucsal.ted.disciplinas.api;

public record DisciplinaResumo(Long id, String sigla, String descricao, Long cursoId, int cargaHoraria,
                               Long docenteId, boolean ativa) {
}
