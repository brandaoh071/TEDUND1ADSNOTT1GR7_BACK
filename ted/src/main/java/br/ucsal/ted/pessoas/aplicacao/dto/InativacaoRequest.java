package br.ucsal.ted.pessoas.aplicacao.dto;

import br.ucsal.ted.pessoas.dominio.MotivoInativacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InativacaoRequest(@NotNull MotivoInativacao motivo, @Size(max = 500) String descricao) {
}
