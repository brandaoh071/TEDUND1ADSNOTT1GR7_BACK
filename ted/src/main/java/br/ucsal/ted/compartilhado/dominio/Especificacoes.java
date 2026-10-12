package br.ucsal.ted.compartilhado.dominio;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class Especificacoes {

    private Especificacoes() {
    }

    public static <T> Specification<T> igual(String atributo, Object valor) {
        if (valor == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(caminho(root, atributo), valor);
    }

    public static <T> Specification<T> contem(String atributo, String texto) {
        if (texto == null || texto.isBlank()) {
            return Specification.unrestricted();
        }
        String padrao = "%" + texto.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(caminho(root, atributo).as(String.class)), padrao);
    }

    public static <T> Specification<T> contemEmAlgum(String texto, String... atributos) {
        if (texto == null || texto.isBlank()) {
            return Specification.unrestricted();
        }
        Specification<T> resultado = Specification.unrestricted();
        boolean primeiro = true;
        for (String atributo : atributos) {
            Specification<T> parcial = contem(atributo, texto);
            resultado = primeiro ? parcial : resultado.or(parcial);
            primeiro = false;
        }
        return resultado;
    }

    public static <T> Specification<T> aPartirDe(String atributo, Instant inicio) {
        if (inicio == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(atributo), inicio);
    }

    public static <T> Specification<T> ate(String atributo, Instant fim) {
        if (fim == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(atributo), fim);
    }

    private static Path<Object> caminho(Root<?> root, String atributo) {
        Path<Object> caminho = null;
        for (String parte : atributo.split("\\.")) {
            caminho = caminho == null ? root.get(parte) : caminho.get(parte);
        }
        return caminho;
    }
}
