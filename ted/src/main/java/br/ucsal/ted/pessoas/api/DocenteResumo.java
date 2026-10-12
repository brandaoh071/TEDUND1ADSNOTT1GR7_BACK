package br.ucsal.ted.pessoas.api;

public record DocenteResumo(Long id, String matricula, String nomeCompleto, Long centroAcademicoId, boolean ativo) {
}
