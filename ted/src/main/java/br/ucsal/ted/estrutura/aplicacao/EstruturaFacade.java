package br.ucsal.ted.estrutura.aplicacao;

import br.ucsal.ted.estrutura.api.CentroAcademicoFicha;
import br.ucsal.ted.estrutura.api.CentroAcademicoResumo;
import br.ucsal.ted.estrutura.api.CursoFicha;
import br.ucsal.ted.estrutura.api.CursoResumo;
import br.ucsal.ted.estrutura.api.EstruturaApi;
import br.ucsal.ted.estrutura.api.IesFicha;
import br.ucsal.ted.estrutura.api.ProReitoriaFicha;
import br.ucsal.ted.estrutura.dominio.CentroAcademico;
import br.ucsal.ted.estrutura.dominio.Curso;
import br.ucsal.ted.estrutura.dominio.Ies;
import br.ucsal.ted.estrutura.infra.CentroAcademicoRepository;
import br.ucsal.ted.estrutura.infra.CursoRepository;
import br.ucsal.ted.estrutura.infra.IesRepository;
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
public class EstruturaFacade implements EstruturaApi {

    private final IesRepository instituicoes;
    private final CentroAcademicoRepository centros;
    private final CursoRepository cursos;

    public EstruturaFacade(IesRepository instituicoes, CentroAcademicoRepository centros, CursoRepository cursos) {
        this.instituicoes = instituicoes;
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

    @Override
    public List<IesFicha> listarEstruturaCompleta() {
        Map<Long, List<CursoFicha>> cursosPorCentro = cursos.findAll(Sort.by("sigla")).stream()
                .collect(Collectors.groupingBy(c -> c.getCentroAcademico().getId(),
                        Collectors.mapping(this::ficha, Collectors.toList())));
        Map<Long, List<CentroAcademicoFicha>> centrosPorIes = centros.findAll(Sort.by("nome")).stream()
                .collect(Collectors.groupingBy(ca -> ca.getIes().getId(), Collectors.mapping(
                        ca -> ficha(ca, cursosPorCentro.getOrDefault(ca.getId(), List.of())),
                        Collectors.toList())));
        return instituicoes.findAll(Sort.by("sigla")).stream()
                .map(ies -> ficha(ies, centrosPorIes.getOrDefault(ies.getId(), List.of())))
                .toList();
    }

    private IesFicha ficha(Ies ies, List<CentroAcademicoFicha> centrosDaIes) {
        List<ProReitoriaFicha> proReitorias = ies.getProReitorias().stream()
                .map(p -> new ProReitoriaFicha(p.getNome(), p.getNomeado()))
                .toList();
        return new IesFicha(ies.getId(), ies.getSigla(), ies.getNome(), ies.getEmailCentral(),
                ies.getTelefoneCentral(), ies.getReitor(), proReitorias, ies.getDataCadastro(), ies.getStatus(),
                centrosDaIes);
    }

    private CentroAcademicoFicha ficha(CentroAcademico ca, List<CursoFicha> cursosDoCentro) {
        return new CentroAcademicoFicha(ca.getId(), ca.getNome(), ca.getEmailCentral(), ca.getTelefoneCentral(),
                ca.getCoordenador(), ca.getDataCadastro(), ca.getStatus(), cursosDoCentro);
    }

    private CursoFicha ficha(Curso curso) {
        List<String> turnos = curso.getTurnos().stream().sorted().map(Enum::name).toList();
        return new CursoFicha(curso.getId(), curso.getSigla(), curso.getDescricao(), curso.getCoordenador(), turnos,
                curso.getDataCadastro(), curso.getStatus());
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
