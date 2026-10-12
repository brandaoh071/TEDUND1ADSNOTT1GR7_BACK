package br.ucsal.ted.compartilhado.excecao;

import java.util.List;

public class RegrasVioladasException extends RegraNegocioException {

    private final List<String> violacoes;

    public RegrasVioladasException(List<String> violacoes) {
        super(String.join("; ", violacoes));
        this.violacoes = List.copyOf(violacoes);
    }

    public List<String> getViolacoes() {
        return violacoes;
    }
}
