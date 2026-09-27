# TEDUND1ADSNOTT1GR7_BACK

API Back-End do **Sistema de Controle de Solicitações de Disciplina** (Cenário TED, versão 03).

Este repositório só atende às requisições do Front-End (`TEDUND1ADSNOTT1GR7_FRONT`) e é o único que acessa o banco PostgreSQL.

---

## 1. Stack

| Item | Tecnologia |
| --- | --- |
| Linguagem | Java 21+ |
| Framework | Spring Boot 3+ |
| Segurança | Spring Security (usuários e perfis no banco) + JWT |
| Persistência | Spring Data JPA + PostgreSQL |
| Migrações de banco | Flyway |
| Modularização | Spring Modulith |
| Validação | Bean Validation (`jakarta.validation`) |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Spring Boot Test, Testcontainers |
| Build | Maven (via `./mvnw`) |

## 2. Restrições do cenário que este projeto cumpre

- Desenvolvido **exclusivamente** em Java + Spring.
- Autenticação e autorização com **Spring Security usando banco de dados**. Não existe usuário em memória.
- Responde **somente** ao Front-End: CORS aceita apenas a origem do Front-End, e toda rota, exceto `/api/auth/login`, exige token válido.
- **Só este projeto** acessa o PostgreSQL.

---

## 3. Estilo arquitetural: Monólito Modular

Na **1ª unidade**, o Back-End é um **monólito modular**: um único projeto e um único deploy, dividido em módulos independentes.
Na **2ª unidade**, cada módulo será extraído para um **microsserviço**. Por isso, os módulos já seguem as fronteiras dos futuros serviços (*Bounded Contexts* do DDD).

### 3.1 Módulos

| Módulo | Responsabilidade | Microsserviço na 2ª unidade |
| --- | --- | --- |
| `identidade` | Usuário, Perfil (ADMIN, COLABORADOR, USUARIO), login, emissão de JWT | `identidade-service` |
| `estrutura` | IES, Centro Acadêmico, Curso | `estrutura-service` |
| `pessoas` | Docente, Discente, histórico de inativação | `pessoas-service` |
| `disciplinas` | Disciplina (vinculada a um curso) | `disciplinas-service` |
| `solicitacoes` | Solicitação, itens, prioridades, validação e lista de espera | `solicitacoes-service` |
| `auditoria` | Registro de todas as ações do sistema | `auditoria-service` (primeiro a ser extraído) |
| `relatorios` | Relatórios de cada perfil com a identificação de quem emitiu | `relatorios-service` |
| `compartilhado` | Tipos comuns: exceções, `Status`, eventos base, utilitários | biblioteca comum |

### 3.2 Regras entre módulos (obrigatórias)

1. **Um módulo não acessa as classes internas de outro.** Ele só usa o pacote `api` do outro módulo, que contém interfaces e DTOs.
2. **Referência entre módulos é feita só por ID.** Exemplo: `Discente` guarda `cursoId`, e não um `@ManyToOne Curso`. Dentro do mesmo módulo, pode usar relacionamento JPA.
3. **Cada módulo tem seu próprio schema no PostgreSQL** (`identidade`, `estrutura`, `pessoas`...). É proibido fazer JOIN entre schemas.
4. **Comunicação assíncrona por eventos de domínio** (`ApplicationEventPublisher` e `@ApplicationModuleListener`). A auditoria só consome eventos.
5. **O teste do Spring Modulith precisa passar.** Ele quebra o build se alguma regra acima for violada:

```java
@Test
void verificaModulos() {
    ApplicationModules.of(Application.class).verify();
}
```

### 3.3 Estrutura de pacotes

```
src/main/java/br/ucsal/ted/
 ├─ Application.java
 ├─ compartilhado/
 ├─ identidade/
 ├─ estrutura/
 ├─ pessoas/
 ├─ disciplinas/
 ├─ solicitacoes/
 │   ├─ api/          ← interface pública do módulo (Facade) + DTOs + eventos publicados
 │   ├─ web/          ← @RestController, request/response DTOs      (adaptador de entrada)
 │   ├─ aplicacao/    ← casos de uso (@Service, @Transactional)
 │   ├─ dominio/      ← entidades, regras de negócio, estados, eventos
 │   └─ infra/        ← repositórios JPA, clientes de outros módulos (adaptador de saída)
 ├─ auditoria/
 └─ relatorios/
```

Dentro de cada módulo, a organização segue uma **Arquitetura Hexagonal leve (Ports & Adapters)**:
- o `dominio` não depende de `web` nem de `infra`;
- a `aplicacao` orquestra os casos de uso;
- `web` e `infra` são adaptadores que podem ser trocados. Na 2ª unidade, o que muda é principalmente o `infra`: chamadas locais passam a ser HTTP ou mensageria.

### 3.4 Fluxo de uma requisição

```
Front-End ──HTTPS/JSON + JWT──▶ SecurityFilterChain (CORS, JWT, perfil)
                                   │
                                   ▼
                             @RestController (web)
                                   │  DTO
                                   ▼
                             Caso de uso (aplicacao) ──▶ API de outro módulo (por ID)
                                   │
                                   ▼
                             Domínio (regras) ──▶ publica evento ──▶ auditoria
                                   │
                                   ▼
                             Repositório JPA (infra) ──▶ PostgreSQL (schema do módulo)
```

---

## 4. Segurança

- **Usuários e perfis ficam no banco** (schema `identidade`), e um `UserDetailsService` próprio lê essas tabelas.
- Senhas são guardadas com **BCrypt**.
- Um usuário pode **acumular perfis**, com relação N:N entre usuário e perfil.
- O login (`POST /api/auth/login`) devolve um **JWT** assinado. As demais rotas usam o `oauth2-resource-server` para validar o token.
- A autorização é feita por rota e por método (`@PreAuthorize("hasRole('ADMIN')")`).
- **CORS** aceita apenas a URL do Front-End, definida em `app.cors.allowed-origin`.
- A sessão é *stateless*: não existe sessão no servidor, o que facilita a migração para microsserviços.

| Perfil | Pode acessar |
| --- | --- |
| `ADMIN` | IES, CA, Curso, Docente, Auditoria, relatórios gerais |
| `COLABORADOR` | Disciplina, Discente, validação de solicitações, relatórios |
| `USUARIO` | Criar e acompanhar a própria solicitação, relatório próprio |

---

## 5. Padrões de projeto

| Padrão | Onde é usado |
| --- | --- |
| **Facade** | Pacote `api` de cada módulo (`EstruturaApi`, `PessoasApi`...), que é a única porta de entrada do módulo |
| **Repository** | Spring Data JPA em `infra` |
| **Service Layer** | Casos de uso em `aplicacao` |
| **DTO + Mapper** | As entidades nunca saem do módulo; a conversão é feita com mappers (MapStruct ou manual) |
| **State** | Status da solicitação: `AGUARDANDO` → `EM_ANALISE` ou `LISTA_ESPERA`, com transições validadas no domínio |
| **Strategy / Specification** | Regras da solicitação: de 2 a 9 disciplinas, prioridade de 1 a 5, no máximo 2 com prioridade máxima. Cada regra é uma `RegraSolicitacao`, e todas são aplicadas em sequência |
| **Observer (Domain Events)** | Eventos como `SolicitacaoCriadaEvent` e `DocenteInativadoEvent`, consumidos pela auditoria |
| **Template Method** | Relatórios: cabeçalho com o emissor, corpo variável e rodapé padrão |
| **Builder** | Montagem de relatórios e de objetos com muitos campos |
| **Adapter** | Clientes de outros módulos em `infra`. Hoje fazem chamada local e na 2ª unidade passam a usar HTTP/Feign |
| **Soft Delete** | Inativação com `status` em vez de exclusão quando há histórico (IES, CA, Curso, Disciplina, Docente, Discente) |
| **Global Exception Handler** | `@RestControllerAdvice` que devolve erros no formato `ProblemDetail` (RFC 9457) |

---

## 6. Convenções da API

- Todas as rotas usam o prefixo `/api`, por exemplo `/api/ies`, `/api/cursos` e `/api/solicitacoes`.
- Recursos no plural, com verbos HTTP: `GET` lista ou consulta, `POST` cria, `PUT` atualiza e `PATCH /{id}/inativar` inativa.
- Não existe `DELETE` nos cadastros que guardam histórico.
- Listas são paginadas (`?page=0&size=20&sort=nome`).
- Os erros seguem o formato `ProblemDetail`, com `type`, `title`, `status`, `detail` e `errors[]`.
- A documentação fica em `/swagger-ui.html`.

## 7. Banco de dados

- PostgreSQL, com **um schema por módulo**.
- Todas as mudanças de estrutura são feitas por **migrações Flyway** (`src/main/resources/db/migration`).
- Os dados de teste do cenário (UCSAL, PUC-Rio, CCET, CTC, cursos...) entram por uma migração de *seed*.

## 8. Auditoria

Toda ação de escrita publica um evento de domínio. O módulo `auditoria` grava em `auditoria.log_auditoria` os campos: usuário, perfil, ação, entidade, id da entidade, data/hora e detalhes.

## 9. Testes

| Tipo | Ferramenta | O que cobre |
| --- | --- | --- |
| Unitário | JUnit 5 + Mockito | Regras de domínio (validação da solicitação, transições de status) |
| Módulos | Spring Modulith | Fronteiras entre módulos |
| Integração | Spring Boot Test + Testcontainers (PostgreSQL) | Repositórios, segurança e endpoints |

---

## 10. Roteiro de migração para microsserviços (2ª unidade)

1. **Strangler Fig**: extrair um módulo por vez, começando pela `auditoria` e terminando em `solicitacoes`.
2. **API Gateway** (Spring Cloud Gateway): passa a ser o único ponto de entrada do Front-End.
3. **Database per Service**: cada schema vira o banco do seu serviço.
4. **Mensageria** (RabbitMQ): substitui os eventos em memória. Usar o **Outbox Pattern** para não perder eventos.
5. **Comunicação síncrona** com OpenFeign e **Circuit Breaker** (Resilience4j).
6. **JWT centralizado**: `identidade-service` emite o token, e os demais serviços só validam.

---

## 11. Como executar

> A seção será preenchida quando o projeto for gerado.

```bash
# subir o PostgreSQL local
docker compose up -d

# executar a API
./mvnw spring-boot:run
```
