INSERT INTO identidade.usuario (login, senha_hash, ativo)
VALUES ('colaborador', '$2a$10$TGXQsoEIrbO2DHe/qe36HutmiZLW4RMmAQDyqqNJFBvz50OaeBWMa', TRUE),
       ('discente', '$2a$10$wlscg5fepUaZqAW.6Fd1F.hxn17GS7o1mhvvKZDsH186yM1O9JDae', TRUE)
ON CONFLICT (login) DO NOTHING;

INSERT INTO identidade.usuario_perfil (usuario_id, perfil_id)
SELECT u.id, p.id
FROM identidade.usuario u
JOIN identidade.perfil p ON (u.login, p.nome) IN (('colaborador', 'COLABORADOR'), ('discente', 'USUARIO'))
ON CONFLICT DO NOTHING;

INSERT INTO pessoas.docente (matricula, nome_completo, email, telefone, centro_academico_id, usuario_id,
                             data_cadastro, criado_por, status)
SELECT 'colaborador', 'Colaborador de Demonstração', 'colaborador@ucsal.exemplo.br', '(71) 99999-0001',
       ca.id, u.id, now(), 'seed', 'ATIVO'
FROM estrutura.centro_academico ca
JOIN identidade.usuario u ON u.login = 'colaborador'
WHERE ca.nome LIKE '%(CCET)'
  AND NOT EXISTS (SELECT 1 FROM pessoas.docente d WHERE d.matricula = 'colaborador' OR d.usuario_id = u.id);

INSERT INTO pessoas.discente (matricula, nome_completo, email, telefone, curso_id, usuario_id,
                              data_cadastro, criado_por, status)
SELECT 'discente', 'Discente de Demonstração', 'discente@ucsal.exemplo.br', '(71) 99999-0002',
       c.id, u.id, now(), 'seed', 'ATIVO'
FROM estrutura.curso c
JOIN estrutura.centro_academico ca ON ca.id = c.centro_academico_id AND ca.nome LIKE '%(CCET)'
JOIN identidade.usuario u ON u.login = 'discente'
WHERE c.sigla = 'BES'
  AND NOT EXISTS (SELECT 1 FROM pessoas.discente d WHERE d.matricula = 'discente' OR d.usuario_id = u.id);
