package br.ucsal.ted.relatorios.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.pessoas.api.PessoaFicha;
import java.time.Instant;

public record PessoaDoRelatorio(
        Long id,
        String matricula,
        String nomeCompleto,
        String email,
        String telefone,
        String vinculo,
        Instant dataCadastro,
        Status status) {

    public static PessoaDoRelatorio de(PessoaFicha pessoa, String vinculo) {
        return new PessoaDoRelatorio(pessoa.id(), pessoa.matricula(), pessoa.nomeCompleto(), pessoa.email(),
                pessoa.telefone(), vinculo, pessoa.dataCadastro(), pessoa.status());
    }
}
