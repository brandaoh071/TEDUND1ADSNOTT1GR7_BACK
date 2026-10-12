package br.ucsal.ted.estrutura.infra;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.estrutura.dominio.CentroAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CentroAcademicoRepository extends JpaRepository<CentroAcademico, Long>,
        JpaSpecificationExecutor<CentroAcademico> {

    boolean existsByNomeIgnoreCaseAndIesId(String nome, Long iesId);

    boolean existsByNomeIgnoreCaseAndIesIdAndIdNot(String nome, Long iesId, Long id);

    boolean existsByIesIdAndStatus(Long iesId, Status status);
}
