package br.ucsal.ted.pessoas.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface PessoasApi {

    Optional<DocenteResumo> buscarDocente(Long id);

    Optional<DocenteResumo> buscarDocentePorUsuario(Long usuarioId);

    Map<Long, DocenteResumo> buscarDocentes(Collection<Long> ids);

    Optional<DiscenteResumo> buscarDiscente(Long id);

    Optional<DiscenteResumo> buscarDiscentePorUsuario(Long usuarioId);

    Map<Long, DiscenteResumo> buscarDiscentes(Collection<Long> ids);
}
