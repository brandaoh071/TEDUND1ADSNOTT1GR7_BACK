package br.ucsal.ted.disciplinas.infra;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.disciplinas.dominio.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long>, JpaSpecificationExecutor<Disciplina> {

    boolean existsBySiglaIgnoreCaseAndCursoId(String sigla, Long cursoId);

    boolean existsBySiglaIgnoreCaseAndCursoIdAndIdNot(String sigla, Long cursoId, Long id);

    List<Disciplina> findByStatusOrderBySigla(Status status);

    List<Disciplina> findByDocenteIdOrCriadoPorOrderBySigla(Long docenteId, String criadoPor);
}
