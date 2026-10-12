package br.ucsal.ted.pessoas.dominio;

import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "docente", schema = "pessoas")
public class Docente extends Pessoa {

    private static final Set<MotivoInativacao> MOTIVOS = EnumSet.of(MotivoInativacao.AFASTAMENTO,
            MotivoInativacao.DESLIGAMENTO, MotivoInativacao.SUSPENSAO_CONTRATUAL, MotivoInativacao.OUTRO);

    @Column(name = "centro_academico_id", nullable = false)
    private Long centroAcademicoId;

    protected Docente() {
    }

    public Docente(String matricula, String nomeCompleto, String email, String telefone, Long centroAcademicoId,
                   Long usuarioId) {
        super(matricula, nomeCompleto, email, telefone, usuarioId);
        vincular(centroAcademicoId);
    }

    public void atualizar(String nomeCompleto, String email, String telefone, Long centroAcademicoId) {
        atualizarContato(nomeCompleto, email, telefone);
        vincular(centroAcademicoId);
    }

    private void vincular(Long centroAcademicoId) {
        if (centroAcademicoId == null) {
            throw new RegraNegocioException("O docente precisa estar vinculado a um centro acadêmico");
        }
        this.centroAcademicoId = centroAcademicoId;
    }

    @Override
    public TipoPessoa tipo() {
        return TipoPessoa.DOCENTE;
    }

    @Override
    public Set<MotivoInativacao> motivosPermitidos() {
        return MOTIVOS;
    }

    public Long getCentroAcademicoId() {
        return centroAcademicoId;
    }
}
