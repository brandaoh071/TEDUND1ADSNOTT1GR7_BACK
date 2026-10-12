package br.ucsal.ted.estrutura.api;

public record CursoResumo(Long id, String sigla, String descricao, Long centroAcademicoId, boolean ativo) {
}
