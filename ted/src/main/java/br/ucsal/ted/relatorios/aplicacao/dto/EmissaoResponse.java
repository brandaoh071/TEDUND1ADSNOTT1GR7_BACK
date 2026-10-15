package br.ucsal.ted.relatorios.aplicacao.dto;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.identidade.api.NomePerfil;
import br.ucsal.ted.relatorios.dominio.Relatorio;
import br.ucsal.ted.relatorios.dominio.TipoRelatorio;

import java.time.Instant;

public record EmissaoResponse(
        Long id,
        TipoRelatorio tipo,
        String titulo,
        String emitidoPor,
        String nomeEmissor,
        NomePerfil perfilEmissor,
        Instant dataEmissao,
        Status filtroStatus) {

    public static EmissaoResponse de(Relatorio relatorio, String nomeEmissor) {
        return new EmissaoResponse(relatorio.getId(), relatorio.getTipo(), relatorio.getTipo().getTitulo(),
                relatorio.getEmitidoPor(), nomeEmissor, relatorio.getPerfilEmissor(), relatorio.getDataEmissao(),
                relatorio.getFiltroStatus());
    }
}
