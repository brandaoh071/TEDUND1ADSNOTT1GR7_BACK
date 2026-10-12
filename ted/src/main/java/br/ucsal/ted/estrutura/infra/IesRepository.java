package br.ucsal.ted.estrutura.infra;

import br.ucsal.ted.estrutura.dominio.Ies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IesRepository extends JpaRepository<Ies, Long>, JpaSpecificationExecutor<Ies> {

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
}
