package br.ucsal.ted.estrutura.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.estrutura.dominio.CentroAcademico;

import java.time.Instant;

public record CentroAcademicoResponse(
        Long id,
        String nome,
        String emailCentral,
        String telefoneCentral,
        String coordenador,
        Long iesId,
        String iesSigla,
        Instant dataCadastro,
        Status status) {

    public static CentroAcademicoResponse de(CentroAcademico ca) {
        return new CentroAcademicoResponse(ca.getId(), ca.getNome(), ca.getEmailCentral(), ca.getTelefoneCentral(),
                ca.getCoordenador(), ca.getIes().getId(), ca.getIes().getSigla(), ca.getDataCadastro(),
                ca.getStatus());
    }
}
