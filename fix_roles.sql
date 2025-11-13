-- Script para corrigir as roles no banco MySQL
-- Execute este script apenas se estiver usando MySQL

-- Desabilitar safe mode temporariamente
SET SQL_SAFE_UPDATES = 0;

-- Remover todas as roles antigas
DELETE FROM user_roles WHERE 1=1;

-- Adicionar role USER para todos os usuários
INSERT INTO user_roles (user_id, role)
SELECT id, 'USER' FROM users;

-- Adicionar role ADMIN para o usuário admin (ajuste o email se necessário)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE email = 'admin123@gmail.com';

-- Opcional: tornar o admin também SELLER para testar o marketplace
INSERT INTO user_roles (user_id, role)
SELECT id, 'SELLER' FROM users WHERE email = 'admin123@gmail.com';

-- Reabilitar safe mode
SET SQL_SAFE_UPDATES = 1;

-- Verificar resultado
SELECT u.email, ur.role 
FROM users u 
LEFT JOIN user_roles ur ON u.id = ur.user_id
ORDER BY u.email, ur.role;
