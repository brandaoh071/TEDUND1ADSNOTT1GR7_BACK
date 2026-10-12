package br.ucsal.ted.pessoas.dominio;

import br.ucsal.ted.compartilhado.dominio.EntidadeCadastral;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.util.Set;

@MappedSuperclass
public abstract class Pessoa extends EntidadeCadastral {

    @Column(nullable = false, unique = true, length = 30, updatable = false)
    private String matricula;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "usuario_id", nullable = false, unique = true, updatable = false)
    private Long usuarioId;

    protected Pessoa() {
    }

    protected Pessoa(String matricula, String nomeCompleto, String email, String telefone, Long usuarioId) {
        this.matricula = matricula.trim();
        this.usuarioId = usuarioId;
        atualizarContato(nomeCompleto, email, telefone);
    }

    public abstract TipoPessoa tipo();

    public abstract Set<MotivoInativacao> motivosPermitidos();

    protected void atualizarContato(String nomeCompleto, String email, String telefone) {
        this.nomeCompleto = nomeCompleto.trim();
        this.email = email.trim().toLowerCase();
        this.telefone = telefone.trim();
    }

    @Override
    public void inativar() {
        throw new RegraNegocioException("Informe o motivo da inativação");
    }

    public HistoricoInativacao inativar(MotivoInativacao motivo, String descricao, String responsavel) {
        if (motivo == null || !motivosPermitidos().contains(motivo)) {
            throw new RegraNegocioException("Motivo de inativação inválido para " + tipo().name().toLowerCase()
                    + ". Permitidos: " + motivosPermitidos());
        }
        if (motivo == MotivoInativacao.OUTRO && (descricao == null || descricao.isBlank())) {
            throw new RegraNegocioException("Descreva o motivo quando a opção for OUTRO");
        }
        super.inativar();
        return new HistoricoInativacao(tipo(), getId(), motivo, descricao == null ? null : descricao.trim(),
                responsavel);
    }

    public String getMatricula() {
        return matricula;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}
