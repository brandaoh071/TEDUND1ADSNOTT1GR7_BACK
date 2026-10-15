package br.ucsal.ted.pessoas.infra;

import br.ucsal.ted.pessoas.dominio.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface PessoaRepository<P extends Pessoa> extends JpaRepository<P, Long>, JpaSpecificationExecutor<P> {

    boolean existsByMatriculaIgnoreCase(String matricula);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Optional<P> findByUsuarioId(Long usuarioId);

    List<P> findByCriadoPorOrderByNomeCompleto(String criadoPor);
}
