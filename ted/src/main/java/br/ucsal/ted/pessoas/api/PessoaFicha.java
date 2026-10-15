package br.ucsal.ted.pessoas.api;

import br.ucsal.ted.compartilhado.dominio.Status;

import java.time.Instant;

public record PessoaFicha(Long id, String matricula, String nomeCompleto, String email, String telefone,
                          Long vinculoId, Instant dataCadastro, Status status) {
}
