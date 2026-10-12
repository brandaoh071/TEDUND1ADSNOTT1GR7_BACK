package br.ucsal.ted.disciplinas.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DisciplinasApi {

    Optional<DisciplinaResumo> buscar(Long id);

    Map<Long, DisciplinaResumo> buscarTodas(Collection<Long> ids);

    List<DisciplinaResumo> listarAtivas();
}
