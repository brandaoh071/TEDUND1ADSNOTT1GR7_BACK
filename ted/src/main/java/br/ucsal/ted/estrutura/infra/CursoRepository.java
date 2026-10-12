package br.ucsal.ted.estrutura.infra;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.estrutura.dominio.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CursoRepository extends JpaRepository<Curso, Long>, JpaSpecificationExecutor<Curso> {

    boolean existsBySiglaIgnoreCaseAndCentroAcademicoId(String sigla, Long centroAcademicoId);

    boolean existsBySiglaIgnoreCaseAndCentroAcademicoIdAndIdNot(String sigla, Long centroAcademicoId, Long id);

    boolean existsByCentroAcademicoIdAndStatus(Long centroAcademicoId, Status status);
}
