package br.ucsal.ted.estrutura.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.estrutura.dominio.Ies;

import java.time.Instant;
import java.util.List;

public record IesResponse(
        Long id,
        String sigla,
        String nome,
        String emailCentral,
        String telefoneCentral,
        String reitor,
        List<ProReitoriaDto> proReitorias,
        Instant dataCadastro,
        Status status) {

    public static IesResponse de(Ies ies) {
        return new IesResponse(ies.getId(), ies.getSigla(), ies.getNome(), ies.getEmailCentral(),
                ies.getTelefoneCentral(), ies.getReitor(),
                ies.getProReitorias().stream().map(ProReitoriaDto::de).toList(),
                ies.getDataCadastro(), ies.getStatus());
    }
}
