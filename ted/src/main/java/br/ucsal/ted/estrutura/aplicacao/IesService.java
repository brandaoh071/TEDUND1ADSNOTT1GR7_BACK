package br.ucsal.ted.estrutura.aplicacao;

import br.ucsal.ted.compartilhado.aplicacao.ServicoCadastral;
import br.ucsal.ted.compartilhado.auditoria.AcaoAuditada;
import br.ucsal.ted.compartilhado.dominio.Especificacoes;
import br.ucsal.ted.compartilhado.dominio.Status;
import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;
import br.ucsal.ted.compartilhado.web.PaginaResponse;
import br.ucsal.ted.estrutura.aplicacao.dto.IesRequest;
import br.ucsal.ted.estrutura.aplicacao.dto.IesResponse;
import br.ucsal.ted.estrutura.aplicacao.dto.ProReitoriaDto;
import br.ucsal.ted.estrutura.dominio.Ies;
import br.ucsal.ted.estrutura.dominio.ProReitoria;
import br.ucsal.ted.estrutura.infra.CentroAcademicoRepository;
import br.ucsal.ted.estrutura.infra.IesRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IesService extends ServicoCadastral<Ies> {

    private final IesRepository iesRepository;
    private final CentroAcademicoRepository centros;

    public IesService(IesRepository iesRepository, CentroAcademicoRepository centros,
                      ApplicationEventPublisher eventos) {
        super(iesRepository, eventos);
        this.iesRepository = iesRepository;
        this.centros = centros;
    }

    @Override
    protected String nomeEntidade() {
        return "IES";
    }

    @Transactional(readOnly = true)
    public PaginaResponse<IesResponse> listar(Status status, String busca, Pageable pageable) {
        Specification<Ies> filtro = Especificacoes.<Ies>igual("status", status)
                .and(Especificacoes.contemEmAlgum(busca, "sigla", "nome"));
        return PaginaResponse.de(iesRepository.findAll(filtro, pageable), IesResponse::de);
    }

    @Transactional(readOnly = true)
    public IesResponse buscar(Long id) {
        return IesResponse.de(buscarEntidade(id));
    }

    @Transactional
    public IesResponse cadastrar(IesRequest request) {
        if (iesRepository.existsBySiglaIgnoreCase(request.sigla().trim())) {
            throw new RegraNegocioException("Já existe uma IES com a sigla " + request.sigla());
        }
        Ies ies = new Ies(request.sigla(), request.nome(), request.emailCentral(), request.telefoneCentral(),
                request.reitor(), proReitorias(request));
        iesRepository.save(ies);
        registrar(AcaoAuditada.CRIAR, ies, ies.getSigla());
        return IesResponse.de(ies);
    }

    @Transactional
    public IesResponse editar(Long id, IesRequest request) {
        if (iesRepository.existsBySiglaIgnoreCaseAndIdNot(request.sigla().trim(), id)) {
            throw new RegraNegocioException("Já existe uma IES com a sigla " + request.sigla());
        }
        Ies ies = buscarEntidade(id);
        ies.atualizar(request.sigla(), request.nome(), request.emailCentral(), request.telefoneCentral(),
                request.reitor(), proReitorias(request));
        registrar(AcaoAuditada.EDITAR, ies, ies.getSigla());
        return IesResponse.de(ies);
    }

    @Override
    protected void antesDeInativar(Ies ies) {
        if (centros.existsByIesIdAndStatus(ies.getId(), Status.ATIVO)) {
            throw new RegraNegocioException(
                    "Não é possível inativar a IES enquanto houver centros acadêmicos ativos vinculados a ela");
        }
    }

    private List<ProReitoria> proReitorias(IesRequest request) {
        return request.proReitorias() == null ? List.of()
                : request.proReitorias().stream().map(ProReitoriaDto::paraDominio).toList();
    }
}
