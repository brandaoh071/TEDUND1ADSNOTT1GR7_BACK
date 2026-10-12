package br.ucsal.ted.pessoas.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.estrutura.api.CentroAcademicoResumo;
import br.ucsal.ted.pessoas.dominio.Docente;

import java.time.Instant;

public record DocenteResponse(
        Long id,
        String matricula,
        String nomeCompleto,
        String email,
        String telefone,
        Long centroAcademicoId,
        String centroAcademicoNome,
        String iesSigla,
        Instant dataCadastro,
        Status status) {

    public static DocenteResponse de(Docente docente, CentroAcademicoResumo ca) {
        return new DocenteResponse(docente.getId(), docente.getMatricula(), docente.getNomeCompleto(),
                docente.getEmail(), docente.getTelefone(), docente.getCentroAcademicoId(),
                ca == null ? null : ca.nome(), ca == null ? null : ca.iesSigla(),
                docente.getDataCadastro(), docente.getStatus());
    }
}
