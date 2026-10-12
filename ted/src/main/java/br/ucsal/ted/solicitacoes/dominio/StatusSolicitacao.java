package br.ucsal.ted.solicitacoes.dominio;

import br.ucsal.ted.compartilhado.excecao.RegraNegocioException;

public enum StatusSolicitacao {

    AGUARDANDO {
        @Override
        StatusSolicitacao aprovar() {
            return EM_ANALISE;
        }

        @Override
        StatusSolicitacao enviarParaListaDeEspera() {
            return LISTA_ESPERA;
        }
    },

    LISTA_ESPERA {
        @Override
        StatusSolicitacao aprovar() {
            return EM_ANALISE;
        }

        @Override
        StatusSolicitacao enviarParaListaDeEspera() {
            throw new RegraNegocioException("A solicitação já está na lista de espera");
        }
    },

    EM_ANALISE {
        @Override
        StatusSolicitacao aprovar() {
            throw new RegraNegocioException("A solicitação já foi aprovada e está em análise");
        }

        @Override
        StatusSolicitacao enviarParaListaDeEspera() {
            throw new RegraNegocioException("Uma solicitação aprovada não pode voltar para a lista de espera");
        }
    };

    abstract StatusSolicitacao aprovar();

    abstract StatusSolicitacao enviarParaListaDeEspera();

    public boolean pendenteDeValidacao() {
        return this != EM_ANALISE;
    }
}
