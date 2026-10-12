package br.ucsal.ted.pessoas.infra;

import br.ucsal.ted.pessoas.dominio.HistoricoInativacao;
import br.ucsal.ted.pessoas.dominio.TipoPessoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoInativacaoRepository extends JpaRepository<HistoricoInativacao, Long> {

    List<HistoricoInativacao> findByTipoPessoaAndPessoaIdOrderByDataHoraDesc(TipoPessoa tipoPessoa, Long pessoaId);
}
