package br.ucsal.ted.solicitacoes.dominio;

import java.time.LocalDate;

public final class SemestreReferencia {

    private SemestreReferencia() {
    }

    public static String subsequente(LocalDate hoje) {
        return hoje.getMonthValue() <= 6 ? hoje.getYear() + ".2" : (hoje.getYear() + 1) + ".1";
    }
}
