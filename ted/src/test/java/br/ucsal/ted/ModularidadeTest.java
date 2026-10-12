package br.ucsal.ted;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularidadeTest {

    private final ApplicationModules modulos = ApplicationModules.of(TedApplication.class);

    @Test
    void verificaFronteirasDosModulos() {
        modulos.verify();
    }

    @Test
    void geraDocumentacaoDosModulos() {
        new Documenter(modulos).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
    }
}
