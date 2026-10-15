package br.ucsal.ted.relatorios.dominio;

public enum TipoRelatorio {
    ESTRUTURA_ACADEMICA("Relatório da estrutura acadêmica"),
    DOCENTES("Relatório do cadastro docente"),
    COLABORADOR_COMPLETO("Relatório completo do colaborador"),
    DISCENTE_COMPLETO("Relatório completo do discente");

    private final String titulo;

    TipoRelatorio(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
