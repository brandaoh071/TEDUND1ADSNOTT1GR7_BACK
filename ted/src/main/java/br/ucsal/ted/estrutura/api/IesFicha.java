package br.ucsal.ted.estrutura.api;

import br.ucsal.ted.compartilhado.dominio.Status;

import java.time.Instant;
import java.util.List;

public record IesFicha(Long id, String sigla, String nome, String emailCentral, String telefoneCentral,
                       String reitor, List<ProReitoriaFicha> proReitorias, Instant dataCadastro, Status status,
                       List<CentroAcademicoFicha> centrosAcademicos) {
}
