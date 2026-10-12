package br.ucsal.ted.estrutura.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.estrutura.dominio.CentroAcademico;
import br.ucsal.ted.estrutura.dominio.Curso;
import br.ucsal.ted.estrutura.dominio.Turno;

import java.time.Instant;
import java.util.List;

public record CursoResponse(
        Long id,
        String sigla,
        String descricao,
        String coordenador,
        List<Turno> turnos,
        Long centroAcademicoId,
        String centroAcademicoNome,
        Long iesId,
        String iesSigla,
        Instant dataCadastro,
        Status status) {

    public static CursoResponse de(Curso curso) {
        CentroAcademico ca = curso.getCentroAcademico();
        return new CursoResponse(curso.getId(), curso.getSigla(), curso.getDescricao(), curso.getCoordenador(),
                curso.getTurnos().stream().sorted().toList(), ca.getId(), ca.getNome(), ca.getIes().getId(),
                ca.getIes().getSigla(), curso.getDataCadastro(), curso.getStatus());
    }
}
