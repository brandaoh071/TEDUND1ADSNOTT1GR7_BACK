package br.ucsal.ted.compartilhado.excecao;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(recurso + " com id " + id + " não encontrado(a)");
    }
}
