package br.ucsal.ted.identidade.infra;

import br.ucsal.ted.identidade.api.NomePerfil;
import br.ucsal.ted.identidade.dominio.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Set;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    Set<Perfil> findByNomeIn(Collection<NomePerfil> nomes);
}
