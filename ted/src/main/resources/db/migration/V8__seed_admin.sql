INSERT INTO identidade.usuario (login, senha_hash, ativo)
VALUES ('admin', '$2a$10$3AUrDFnoCfpHIihP.IgFD.LP1JflONVICWhayEM6IkVUkwhhD6zSu', TRUE);

INSERT INTO identidade.usuario_perfil (usuario_id, perfil_id)
SELECT u.id, p.id FROM identidade.usuario u, identidade.perfil p
WHERE u.login = 'admin' AND p.nome = 'ADMIN';
