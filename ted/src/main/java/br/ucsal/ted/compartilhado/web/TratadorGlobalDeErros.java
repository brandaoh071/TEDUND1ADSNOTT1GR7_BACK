package br.ucsal.ted.compartilhado.web;

import br.ucsal.ted.compartilhado.excecao.RecursoNaoEncontradoException;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.excecao.RegrasVioladasException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class TratadorGlobalDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail tratarRegraNegocio(RegraNegocioException ex) {
        ProblemDetail corpo = problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", ex.getMessage());
        if (ex instanceof RegrasVioladasException regras) {
            corpo.setProperty("violacoes", regras.getViolacoes());
        }
        return corpo;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail tratarConflito(OptimisticLockingFailureException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito de atualização",
                "O registro foi alterado por outro usuário. Atualize a tela e tente novamente.");
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail tratarOrdenacaoInvalida(PropertyReferenceException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "Não é possível ordenar pelo campo " + ex.getPropertyName());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail tratarAutenticacao(AuthenticationException ex) {
        return problema(HttpStatus.UNAUTHORIZED, "Não autenticado", "Login ou senha inválidos");
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ProblemDetail tratarAcessoNegado(RuntimeException ex) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado",
                "Seu perfil não tem permissão para executar esta ação");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        List<Map<String, String>> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> Map.of("campo", e.getField(), "mensagem", String.valueOf(e.getDefaultMessage())))
                .toList();
        ProblemDetail corpo = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos");
        corpo.setProperty("errors", erros);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroInesperado(Exception ex) {
        logger.error("Erro inesperado", ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
