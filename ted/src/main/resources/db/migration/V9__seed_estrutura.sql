INSERT INTO estrutura.ies (sigla, nome, email_central, telefone_central, reitor, data_cadastro, criado_por, status)
VALUES ('UCSAL', 'Universidade Católica do Salvador', 'contato@ucsal.exemplo.br', '(71) 0000-0000',
        'Reitor(a) da UCSAL', now(), 'seed', 'ATIVO'),
       ('PUC-RIO', 'Pontifícia Universidade Católica do Rio de Janeiro', 'contato@puc-rio.exemplo.br', '(21) 0000-0001',
        'Reitor(a) da PUC-Rio', now(), 'seed', 'ATIVO');

INSERT INTO estrutura.pro_reitoria (ies_id, ordem, nome, nomeado)
SELECT id, 0, 'Pró-Reitoria de Graduação', 'Pró-Reitor(a) de Graduação' FROM estrutura.ies WHERE sigla = 'UCSAL'
UNION ALL
SELECT id, 1, 'Pró-Reitoria de Pesquisa e Pós-Graduação', 'Pró-Reitor(a) de Pesquisa' FROM estrutura.ies WHERE sigla = 'UCSAL'
UNION ALL
SELECT id, 0, 'Vice-Reitoria Acadêmica', 'Vice-Reitor(a) Acadêmico(a)' FROM estrutura.ies WHERE sigla = 'PUC-RIO';

INSERT INTO estrutura.centro_academico (nome, email_central, telefone_central, coordenador, ies_id, data_cadastro, criado_por, status)
SELECT 'Escola de Engenharias e Ciências Tecnológicas (CCET)', 'ccet@ucsal.exemplo.br', '(71) 0000-0010',
       'Coordenador(a) do CCET', id, now(), 'seed', 'ATIVO' FROM estrutura.ies WHERE sigla = 'UCSAL'
UNION ALL
SELECT 'Escola de Ciências Sociais e Aplicadas (CCSA)', 'ccsa@ucsal.exemplo.br', '(71) 0000-0020',
       'Coordenador(a) do CCSA', id, now(), 'seed', 'ATIVO' FROM estrutura.ies WHERE sigla = 'UCSAL'
UNION ALL
SELECT 'Centro Técnico Científico (CTC)', 'ctc@puc-rio.exemplo.br', '(21) 0000-0100',
       'Coordenador(a) do CTC', id, now(), 'seed', 'ATIVO' FROM estrutura.ies WHERE sigla = 'PUC-RIO';

INSERT INTO estrutura.curso (sigla, descricao, coordenador, centro_academico_id, data_cadastro, criado_por, status)
SELECT c.sigla, c.descricao, c.coordenador, ca.id, now(), 'seed', 'ATIVO'
FROM (VALUES ('BES', 'Bacharelado em Engenharia de Software', 'Coordenador(a) de Engenharia de Software', 'CCET'),
             ('DIR', 'Direito', 'Coordenador(a) de Direito', 'CCET'),
             ('ADS', 'Tecnológico em Análise e Desenvolvimento de Sistemas', 'Coordenador(a) de ADS', 'CCET'),
             ('CC', 'Ciência da Computação', 'Coordenador(a) de Ciência da Computação', 'CTC'),
             ('ECA', 'Engenharia de Controle e Automação', 'Coordenador(a) de Controle e Automação', 'CTC'))
         AS c(sigla, descricao, coordenador, ca_sigla)
JOIN estrutura.centro_academico ca ON ca.nome LIKE '%(' || c.ca_sigla || ')';

INSERT INTO estrutura.curso_turno (curso_id, turno)
SELECT id, 'NOTURNO' FROM estrutura.curso WHERE sigla IN ('BES', 'ADS', 'DIR')
UNION ALL
SELECT id, 'MATUTINO' FROM estrutura.curso WHERE sigla IN ('BES', 'DIR', 'CC', 'ECA')
UNION ALL
SELECT id, 'INTEGRAL' FROM estrutura.curso WHERE sigla IN ('CC', 'ECA');
