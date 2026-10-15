package br.ucsal.ted.disciplinas.api;

import br.ucsal.ted.compartilhado.dominio.Status;

import java.time.Instant;

public record DisciplinaFicha(Long id, String sigla, String descricao, Long cursoId, int cargaHoraria,
                              Instant dataCadastro, Status status) {
}
