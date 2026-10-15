package br.ucsal.ted.estrutura.api;

import br.ucsal.ted.compartilhado.dominio.Status;

import java.time.Instant;
import java.util.List;

public record CentroAcademicoFicha(Long id, String nome, String emailCentral, String telefoneCentral,
                                   String coordenador, Instant dataCadastro, Status status,
                                   List<CursoFicha> cursos) {
}
