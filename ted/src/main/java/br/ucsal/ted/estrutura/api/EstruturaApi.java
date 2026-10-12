package br.ucsal.ted.estrutura.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface EstruturaApi {

    Optional<CentroAcademicoResumo> buscarCentroAcademico(Long id);

    Optional<CursoResumo> buscarCurso(Long id);

    Map<Long, CentroAcademicoResumo> buscarCentrosAcademicos(Collection<Long> ids);

    Map<Long, CursoResumo> buscarCursos(Collection<Long> ids);
}
