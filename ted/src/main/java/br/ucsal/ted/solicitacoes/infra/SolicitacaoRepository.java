package br.ucsal.ted.solicitacoes.infra;

import br.ucsal.ted.solicitacoes.dominio.Solicitacao;
import br.ucsal.ted.solicitacoes.dominio.StatusSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long>,
        JpaSpecificationExecutor<Solicitacao> {

    boolean existsByDiscenteIdAndSemestreReferencia(Long discenteId, String semestreReferencia);

    Page<Solicitacao> findByDiscenteId(Long discenteId, Pageable pageable);

    Optional<Solicitacao> findFirstByCentroAcademicoIdAndStatusOrderByDataSolicitacaoAscIdAsc(
            Long centroAcademicoId, StatusSolicitacao status);
}
