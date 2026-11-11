# Instalação e configuração do Rota 360

## Pré-requisitos

Antes de iniciar a instalação, é necessário possuir as seguintes ferramentas instaladas:

- [Node.js](https://nodejs.org/en/download)
- [PostgreSQL](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)
- [Git Bash](https://git-scm.com/install/windows)
- [Visual Studio Code](https://code.visualstudio.com/download)

**Configuração inicial do PostgreSQL:**

Durante a instalação, definir:
- Porta: `5432`
- Usuário padrão: `postgres`
- Senha: `postgres`

> Essas credenciais são utilizadas apenas para o ambiente local de desenvolvimento.

## 1. Clonagem ou obtenção do projeto

Abra o Git Bash e execute:

```bash
git clone https://github.com/rotasmartsb/rota360.git
```

## 2. Instalação das dependências

Abra o CMD e acesse a pasta do projeto:

```cmd
cd /d "%USERPROFILE%\rota360"
```

Execute os comandos para instalar as dependências:

```cmd
npm install
npm run install:all
```

## 3. Configuração do backend/.env

Na pasta raiz do projeto, execute:

```cmd
copy backend\.env.example backend\.env
```

Abra o projeto no Visual Studio Code e edite o arquivo `backend/.env`.

Confirme as seguintes configurações:

```env
NODE_ENV=development

PG_HOST=localhost
PG_PORT=5432
PG_DB=rota360_db
PG_USER=rota360_user
PG_PASS=postgres
PG_SCHEMA=rota360
```

Configure também os seguintes campos:

```env
JWT_SECRET=<string longa e aleatória>
CRYPTO_SECRET=<chave com pelo menos 32 caracteres>
DATABASE_URL=postgresql://rota360_user:postgres@localhost:5432/rota360_db
ADMIN_PASSWORD=<senha-forte>
```

**Geração das chaves de segurança**

Para gerar os valores de `JWT_SECRET` e `CRYPTO_SECRET`, abra o CMD e execute o comando abaixo duas vezes, utilizando uma chave diferente para cada variável:

```cmd
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

Copie os valores gerados e substitua os respectivos campos no arquivo `.env`.

## 4. Modificação do package.json

No arquivo `backend/package.json`, substitua a linha 7, correspondente ao script `dev`, pelo seguinte comando:

```json
"dev": "nodemon --watch src --watch tsconfig.json --ext ts,json --exec \"node --max-old-space-size=8192 -r ts-node/register src/server.ts\"",
```

Salve as alterações.

## 5. Instalação e configuração do PostgreSQL

Abra um novo CMD e acesse a pasta de instalação do PostgreSQL:

```cmd
cd /d "C:\Program Files\PostgreSQL\18\bin"
```

Confirme a instalação executando:

```cmd
psql --version
```

## 6. Criação do banco de dados

No mesmo CMD, execute:

```cmd
createdb -U postgres rota360_db
```

Quando solicitado, informe a senha do usuário `postgres`.

## 7. Importação da estrutura do banco de dados

Baixe o arquivo `estrutura_rota360.sql` disponibilizado para a instalação.

Execute o comando abaixo para importar a estrutura:

```cmd
psql -U postgres -d rota360_db -f "%USERPROFILE%\Downloads\estrutura_rota360.sql"
```

> Caso o navegador tenha acrescentado `.txt` ao nome do arquivo, ajuste o caminho para corresponder ao nome real do arquivo baixado.

## 8. Criação do usuário rota360_user

Abra o pgAdmin e acesse:

**Servers → PostgreSQL → Login/Group Roles**

1. Clique com o botão direito em `Login/Group Roles`.
2. Selecione **Create → Login/Group Role**.
3. Na aba **General**, defina o nome como `rota360_user`.
4. Na aba **Definition**, defina a senha como `postgres`.
5. Salve as alterações.

## 9. Transferência de propriedade dos objetos do banco

Abra um CMD e acesse o banco de dados:

```cmd
psql -U postgres -d rota360_db
```

Execute os comandos abaixo dentro do terminal do PostgreSQL.

**Transferência de propriedade dos schemas:**

```sql
ALTER SCHEMA rota360 OWNER TO rota360_user;
ALTER SCHEMA pet OWNER TO rota360_user;
```

**Transferência de propriedade das tabelas:**

```sql
DO $$
DECLARE obj RECORD;
BEGIN
    FOR obj IN
        SELECT schemaname, tablename
        FROM pg_tables
        WHERE schemaname IN ('rota360', 'pet')
    LOOP
        EXECUTE format(
            'ALTER TABLE %I.%I OWNER TO rota360_user',
            obj.schemaname,
            obj.tablename
        );
    END LOOP;
END $$;
```

**Transferência de propriedade das sequences:**

```sql
DO $$
DECLARE obj RECORD;
BEGIN
    FOR obj IN
        SELECT
            n.nspname AS schemaname,
            c.relname AS sequencename
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = 'S'
          AND n.nspname IN ('rota360', 'pet')
    LOOP
        EXECUTE format(
            'ALTER SEQUENCE %I.%I OWNER TO rota360_user',
            obj.schemaname,
            obj.sequencename
        );
    END LOOP;
END $$;
```

**Transferência de propriedade das funções e procedures:**

```sql
DO $$
DECLARE obj RECORD;
BEGIN
    FOR obj IN
        SELECT
            n.nspname AS schema_name,
            p.proname AS routine_name,
            p.prokind,
            pg_get_function_identity_arguments(p.oid) AS arguments
        FROM pg_proc p
        JOIN pg_namespace n ON n.oid = p.pronamespace
        WHERE n.nspname IN ('rota360', 'pet')
          AND p.prokind IN ('f', 'p')
    LOOP
        EXECUTE format(
            'ALTER %s %I.%I(%s) OWNER TO rota360_user',
            CASE
                WHEN obj.prokind = 'p' THEN 'PROCEDURE'
                ELSE 'FUNCTION'
            END,
            obj.schema_name,
            obj.routine_name,
            obj.arguments
        );
    END LOOP;
END $$;
```

**Transferência de propriedade das views:**

```sql
DO $$
DECLARE obj RECORD;
BEGIN
    FOR obj IN
        SELECT schemaname, viewname
        FROM pg_views
        WHERE schemaname IN ('rota360', 'pet')
    LOOP
        EXECUTE format(
            'ALTER VIEW %I.%I OWNER TO rota360_user',
            obj.schemaname,
            obj.viewname
        );
    END LOOP;
END $$;
```

**Concessão de permissões:**

```sql
GRANT CONNECT, CREATE ON DATABASE rota360_db TO rota360_user;

GRANT ALL ON SCHEMA rota360, pet TO rota360_user;

ALTER USER rota360_user WITH PASSWORD 'postgres';
```

Após executar todos os comandos, encerre a conexão com:

```sql
\q
```

## 10. Inicialização do sistema

Abra um CMD, acesse a pasta raiz do projeto e execute:

```cmd
cd /d "%USERPROFILE%\rota360"
npm run dev
```

Após a inicialização, o frontend estará disponível em:

```text
http://localhost:5173
```

O backend será executado na porta `3001`.
