package br.ucsal.ted.compartilhado.aplicacao;

import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.auditoria.EventoAuditoria;
import br.ucsal.ted.compartilhado.dominio.EntidadeCadastral;
import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class ServicoCadastral<E extends EntidadeCadastral> {

    protected final JpaRepository<E, Long> repositorio;
    protected final ApplicationEventPublisher eventos;

    protected ServicoCadastral(JpaRepository<E, Long> repositorio, ApplicationEventPublisher eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    protected abstract String nomeEntidade();

    protected void antesDeInativar(E entidade) {
    }

    protected void depoisDeInativar(E entidade) {
    }

    protected void antesDeReativar(E entidade) {
    }

    protected void depoisDeReativar(E entidade) {
    }

    @Transactional
    public void inativar(Long id) {
        E entidade = buscarEntidade(id);
        antesDeInativar(entidade);
        entidade.inativar();
        depoisDeInativar(entidade);
        registrar(AcaoAuditada.INATIVAR, entidade, null);
    }

    @Transactional
    public void reativar(Long id) {
        E entidade = buscarEntidade(id);
        antesDeReativar(entidade);
        entidade.reativar();
        depoisDeReativar(entidade);
        registrar(AcaoAuditada.REATIVAR, entidade, null);
    }

    protected E buscarEntidade(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(nomeEntidade(), id));
    }

    protected void registrar(AcaoAuditada acao, E entidade, String detalhes) {
        eventos.publishEvent(EventoAuditoria.de(acao, nomeEntidade(), entidade.getId(), detalhes));
    }
}
