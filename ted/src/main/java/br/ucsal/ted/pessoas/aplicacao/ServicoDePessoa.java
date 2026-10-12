package br.ucsal.ted.pessoas.aplicacao;

import br.ucsal.ted.compartilhado.aplicacao.ServicoCadastral;
import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.seguranca.UsuarioLogado;
import br.ucsal.ted.identidade.api.NomePerfil;
import br.ucsal.ted.identidade.api.UsuarioApi;
import br.ucsal.ted.pessoas.aplicacao.dto.HistoricoInativacaoResponse;
import br.ucsal.ted.pessoas.dominio.HistoricoInativacao;
import br.ucsal.ted.pessoas.dominio.MotivoInativacao;
import br.ucsal.ted.pessoas.dominio.Pessoa;
import br.ucsal.ted.pessoas.infra.HistoricoInativacaoRepository;
import br.ucsal.ted.pessoas.infra.PessoaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public abstract class ServicoDePessoa<P extends Pessoa> extends ServicoCadastral<P> {

    protected final PessoaRepository<P> pessoas;
    private final HistoricoInativacaoRepository historicos;
    private final UsuarioApi usuarioApi;

    protected ServicoDePessoa(PessoaRepository<P> pessoas, HistoricoInativacaoRepository historicos,
                              UsuarioApi usuarioApi, ApplicationEventPublisher eventos) {
        super(pessoas, eventos);
        this.pessoas = pessoas;
        this.historicos = historicos;
        this.usuarioApi = usuarioApi;
    }

    protected abstract NomePerfil perfilDaConta();

    @Transactional
    public void inativar(Long id, MotivoInativacao motivo, String descricao) {
        P pessoa = buscarEntidade(id);
        HistoricoInativacao historico = pessoa.inativar(motivo, descricao, UsuarioLogado.atual().login());
        historicos.save(historico);
        usuarioApi.desativarConta(pessoa.getUsuarioId());
        registrar(AcaoAuditada.INATIVAR, pessoa, "Motivo: " + motivo + (descricao == null ? "" : " - " + descricao));
    }

    @Transactional(readOnly = true)
    public List<HistoricoInativacaoResponse> historico(Long id) {
        P pessoa = buscarEntidade(id);
        return historicos.findByTipoPessoaAndPessoaIdOrderByDataHoraDesc(pessoa.tipo(), pessoa.getId()).stream()
                .map(HistoricoInativacaoResponse::de)
                .toList();
    }

    @Override
    protected void depoisDeReativar(P pessoa) {
        usuarioApi.reativarConta(pessoa.getUsuarioId());
    }

    protected Long criarConta(String matricula, String email, String senha) {
        if (senha == null || senha.isBlank()) {
            throw new RegraNegocioException("Informe a senha inicial de acesso");
        }
        if (pessoas.existsByMatriculaIgnoreCase(matricula.trim())) {
            throw new RegraNegocioException("Já existe um cadastro com a matrícula " + matricula);
        }
        validarEmail(email, null);
        return usuarioApi.criarConta(matricula.trim(), senha, Set.of(perfilDaConta()));
    }

    protected void validarEdicao(P pessoa, String matricula, String email) {
        if (!pessoa.getMatricula().equalsIgnoreCase(matricula.trim())) {
            throw new RegraNegocioException("A matrícula não pode ser alterada");
        }
        validarEmail(email, pessoa.getId());
    }

    private void validarEmail(String email, Long id) {
        boolean emUso = id == null
                ? pessoas.existsByEmailIgnoreCase(email.trim())
                : pessoas.existsByEmailIgnoreCaseAndIdNot(email.trim(), id);
        if (emUso) {
            throw new RegraNegocioException("O e-mail " + email + " já está em uso");
        }
    }
}
