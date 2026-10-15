package br.ucsal.ted.estrutura.api;

import br.ucsal.ted.compartilhado.dominio.Status;

import java.time.Instant;
import java.util.List;

public record CursoFicha(Long id, String sigla, String descricao, String coordenador, List<String> turnos,
                         Instant dataCadastro, Status status) {
}
