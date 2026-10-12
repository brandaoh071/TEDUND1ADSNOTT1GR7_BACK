package br.ucsal.ted.pessoas.dominio;

import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "discente", schema = "pessoas")
public class Discente extends Pessoa {

    private static final Set<MotivoInativacao> MOTIVOS = EnumSet.of(MotivoInativacao.AFASTAMENTO,
            MotivoInativacao.DESLIGAMENTO, MotivoInativacao.DESISTENCIA, MotivoInativacao.OUTRO);

    @Column(name = "curso_id", nullable = false)
    private Long cursoId;

    protected Discente() {
    }

    public Discente(String matricula, String nomeCompleto, String email, String telefone, Long cursoId,
                    Long usuarioId) {
        super(matricula, nomeCompleto, email, telefone, usuarioId);
        vincular(cursoId);
    }

    public void atualizar(String nomeCompleto, String email, String telefone, Long cursoId) {
        atualizarContato(nomeCompleto, email, telefone);
        vincular(cursoId);
    }

    private void vincular(Long cursoId) {
        if (cursoId == null) {
            throw new RegraNegocioException("O discente precisa estar vinculado a um curso");
        }
        this.cursoId = cursoId;
    }

    @Override
    public TipoPessoa tipo() {
        return TipoPessoa.DISCENTE;
    }

    @Override
    public Set<MotivoInativacao> motivosPermitidos() {
        return MOTIVOS;
    }

    public Long getCursoId() {
        return cursoId;
    }
}
