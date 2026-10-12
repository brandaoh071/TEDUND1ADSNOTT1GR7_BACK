package br.ucsal.ted.estrutura.aplicacao;

import br.ucsal.ted.estrutura.api.CentroAcademicoResumo;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.estrutura.api.EstruturaApi;
import br.ucsal.ted.estrutura.dominio.CentroAcademico;
import br.ucsal.ted.estrutura.dominio.Curso;
import br.ucsal.ted.estrutura.infra.CentroAcademicoRepository;
import br.ucsal.ted.estrutura.infra.CursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EstruturaFacade implements EstruturaApi {

    private final CentroAcademicoRepository centros;
    private final CursoRepository cursos;

    public EstruturaFacade(CentroAcademicoRepository centros, CursoRepository cursos) {
        this.centros = centros;
        this.cursos = cursos;
    }

    @Override
    public Optional<CentroAcademicoResumo> buscarCentroAcademico(Long id) {
        return centros.findById(id).map(this::resumo);
    }

    @Override
    public Optional<CursoResumo> buscarCurso(Long id) {
        return cursos.findById(id).map(this::resumo);
    }

    @Override
    public Map<Long, CentroAcademicoResumo> buscarCentrosAcademicos(Collection<Long> ids) {
        return centros.findAllById(ids).stream().map(this::resumo)
                .collect(Collectors.toMap(CentroAcademicoResumo::id, Function.identity()));
    }

    @Override
    public Map<Long, CursoResumo> buscarCursos(Collection<Long> ids) {
        return cursos.findAllById(ids).stream().map(this::resumo)
                .collect(Collectors.toMap(CursoResumo::id, Function.identity()));
    }

    private CentroAcademicoResumo resumo(CentroAcademico ca) {
        return new CentroAcademicoResumo(ca.getId(), ca.getNome(), ca.getIes().getId(), ca.getIes().getSigla(),
                ca.estaAtiva());
    }

    private CursoResumo resumo(Curso curso) {
        return new CursoResumo(curso.getId(), curso.getSigla(), curso.getDescricao(),
                curso.getCentroAcademico().getId(), curso.estaAtiva());
    }
}
