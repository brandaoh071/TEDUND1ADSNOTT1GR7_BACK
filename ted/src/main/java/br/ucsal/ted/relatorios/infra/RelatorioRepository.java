package br.ucsal.ted.relatorios.infra;

import br.ucsal.ted.relatorios.dominio.Relatorio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {
}
