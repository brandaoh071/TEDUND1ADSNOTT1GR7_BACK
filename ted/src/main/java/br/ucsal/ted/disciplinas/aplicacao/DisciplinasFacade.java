package br.ucsal.ted.disciplinas.aplicacao;

import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.disciplinas.api.DisciplinaResumo;
import br.ucsal.ted.disciplinas.api.DisciplinasApi;
import br.ucsal.ted.disciplinas.dominio.Disciplina;
import br.ucsal.ted.disciplinas.infra.DisciplinaRepository;
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
public class DisciplinasFacade implements DisciplinasApi {

    private final DisciplinaRepository disciplinas;

    public DisciplinasFacade(DisciplinaRepository disciplinas) {
        this.disciplinas = disciplinas;
    }

    @Override
    public Optional<DisciplinaResumo> buscar(Long id) {
        return disciplinas.findById(id).map(this::resumo);
    }

    @Override
    public Map<Long, DisciplinaResumo> buscarTodas(Collection<Long> ids) {
        return disciplinas.findAllById(ids).stream().map(this::resumo)
                .collect(Collectors.toMap(DisciplinaResumo::id, Function.identity()));
    }

    @Override
    public List<DisciplinaResumo> listarAtivas() {
        return disciplinas.findByStatusOrderBySigla(Status.ATIVO).stream().map(this::resumo).toList();
    }

    private DisciplinaResumo resumo(Disciplina d) {
        return new DisciplinaResumo(d.getId(), d.getSigla(), d.getDescricao(), d.getCursoId(), d.getCargaHoraria(),
                d.getDocenteId(), d.estaAtiva());
    }
}
