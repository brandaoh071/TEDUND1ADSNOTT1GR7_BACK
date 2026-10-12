package br.ucsal.ted.pessoas.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.pessoas.dominio.Discente;

import java.time.Instant;

public record DiscenteResponse(
        Long id,
        String matricula,
        String nomeCompleto,
        String email,
        String telefone,
        Long cursoId,
        String cursoSigla,
        String cursoDescricao,
        Instant dataCadastro,
        Status status) {

    public static DiscenteResponse de(Discente discente, CursoResumo curso) {
        return new DiscenteResponse(discente.getId(), discente.getMatricula(), discente.getNomeCompleto(),
                discente.getEmail(), discente.getTelefone(), discente.getCursoId(),
                curso == null ? null : curso.sigla(), curso == null ? null : curso.descricao(),
                discente.getDataCadastro(), discente.getStatus());
    }
}
