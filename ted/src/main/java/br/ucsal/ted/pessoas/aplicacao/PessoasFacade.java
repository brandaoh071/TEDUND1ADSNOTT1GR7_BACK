package br.ucsal.ted.pessoas.aplicacao;

import br.ucsal.ted.pessoas.api.DiscenteResumo;
import br.ucsal.ted.pessoas.api.DocenteResumo;
import br.ucsal.ted.pessoas.api.PessoaFicha;
import br.ucsal.ted.pessoas.api.PessoasApi;
import br.ucsal.ted.pessoas.dominio.Discente;
import br.ucsal.ted.pessoas.dominio.Docente;
import br.ucsal.ted.pessoas.dominio.Pessoa;
import br.ucsal.ted.pessoas.infra.DiscenteRepository;
import br.ucsal.ted.pessoas.infra.DocenteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PessoasFacade implements PessoasApi {

    private final DocenteRepository docentes;
    private final DiscenteRepository discentes;

    public PessoasFacade(DocenteRepository docentes, DiscenteRepository discentes) {
        this.docentes = docentes;
        this.discentes = discentes;
    }

    @Override
    public Optional<DocenteResumo> buscarDocente(Long id) {
        return docentes.findById(id).map(this::resumo);
    }

    @Override
    public Optional<DocenteResumo> buscarDocentePorUsuario(Long usuarioId) {
        return usuarioId == null ? Optional.empty() : docentes.findByUsuarioId(usuarioId).map(this::resumo);
    }

    @Override
    public Map<Long, DocenteResumo> buscarDocentes(Collection<Long> ids) {
        return docentes.findAllById(ids).stream().map(this::resumo)
                .collect(Collectors.toMap(DocenteResumo::id, Function.identity()));
    }

    @Override
    public Optional<DiscenteResumo> buscarDiscente(Long id) {
        return discentes.findById(id).map(this::resumo);
    }

    @Override
    public Optional<DiscenteResumo> buscarDiscentePorUsuario(Long usuarioId) {
        return usuarioId == null ? Optional.empty() : discentes.findByUsuarioId(usuarioId).map(this::resumo);
    }

    @Override
    public Map<Long, DiscenteResumo> buscarDiscentes(Collection<Long> ids) {
        return discentes.findAllById(ids).stream().map(this::resumo)
                .collect(Collectors.toMap(DiscenteResumo::id, Function.identity()));
    }

    @Override
    public List<PessoaFicha> listarFichasDeDocentes() {
        return docentes.findAll(Sort.by("nomeCompleto")).stream()
                .map(d -> ficha(d, d.getCentroAcademicoId()))
                .toList();
    }

    @Override
    public Optional<PessoaFicha> buscarFichaDoDocentePorUsuario(Long usuarioId) {
        return usuarioId == null ? Optional.empty()
                : docentes.findByUsuarioId(usuarioId).map(d -> ficha(d, d.getCentroAcademicoId()));
    }

    @Override
    public Optional<PessoaFicha> buscarFichaDoDiscentePorUsuario(Long usuarioId) {
        return usuarioId == null ? Optional.empty()
                : discentes.findByUsuarioId(usuarioId).map(d -> ficha(d, d.getCursoId()));
    }

    @Override
    public List<PessoaFicha> listarFichasDeDiscentesCadastradosPor(String login) {
        return discentes.findByCriadoPorOrderByNomeCompleto(login).stream()
                .map(d -> ficha(d, d.getCursoId()))
                .toList();
    }

    private PessoaFicha ficha(Pessoa p, Long vinculoId) {
        return new PessoaFicha(p.getId(), p.getMatricula(), p.getNomeCompleto(), p.getEmail(), p.getTelefone(),
                vinculoId, p.getDataCadastro(), p.getStatus());
    }

    private DocenteResumo resumo(Docente d) {
        return new DocenteResumo(d.getId(), d.getMatricula(), d.getNomeCompleto(), d.getCentroAcademicoId(),
                d.estaAtiva());
    }

    private DiscenteResumo resumo(Discente d) {
        return new DiscenteResumo(d.getId(), d.getMatricula(), d.getNomeCompleto(), d.getCursoId(), d.estaAtiva());
    }
}
