package br.ucsal.ted.solicitacoes.aplicacao;

import br.ucsal.ted.solicitacoes.api.SolicitacaoFicha;
import br.ucsal.ted.solicitacoes.api.SolicitacoesApi;
import br.ucsal.ted.solicitacoes.dominio.Solicitacao;
import br.ucsal.ted.solicitacoes.infra.SolicitacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SolicitacoesFacade implements SolicitacoesApi {

    private final SolicitacaoRepository solicitacoes;

    public SolicitacoesFacade(SolicitacaoRepository solicitacoes) {
        this.solicitacoes = solicitacoes;
    }

    @Override
    public List<SolicitacaoFicha> listarDoDiscente(Long discenteId) {
        return solicitacoes.findByDiscenteIdOrderByDataSolicitacaoDesc(discenteId).stream().map(this::ficha).toList();
    }

    @Override
    public List<SolicitacaoFicha> listarValidadasPor(String login) {
        return solicitacoes.findByValidadoPorOrderByDataValidacaoDesc(login).stream().map(this::ficha).toList();
    }

    private SolicitacaoFicha ficha(Solicitacao s) {
        List<SolicitacaoFicha.ItemFicha> itens = s.getItens().stream()
                .map(i -> new SolicitacaoFicha.ItemFicha(i.getDisciplinaId(), i.getPrioridade()))
                .toList();
        return new SolicitacaoFicha(s.getId(), s.getDiscenteId(), s.getSemestreReferencia(), s.getDataSolicitacao(),
                s.getStatus().name(), s.getValidadoPor(), s.getDataValidacao(), s.getObservacao(), itens);
    }
}
