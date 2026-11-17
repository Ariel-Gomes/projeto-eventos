# Sistema de Gerenciamento de Eventos

Aplicação web para divulgação de eventos e gerenciamento de inscrições. Os visitantes podem consultar os eventos disponíveis e realizar ou cancelar uma inscrição. O administrador acessa um painel protegido por código para criar e editar eventos e gerenciar os participantes.

## Funcionalidades

### Área pública

- Listagem de eventos ordenados por data.
- Exibição da data, do total de vagas e das vagas disponíveis.
- Inscrição em um evento com nome e e-mail.
- Bloqueio de novas inscrições quando não há vagas disponíveis.
- Geração de um código individual de cancelamento após a inscrição.
- Cancelamento da inscrição usando e-mail e código; a vaga é liberada novamente.

### Área administrativa

- Acesso por código administrativo.
- Sessão administrativa com expiração após 30 minutos de inatividade.
- Cadastro, edição e exclusão de eventos.
- Definição do título, data/horário e quantidade total de vagas.
- Visualização dos participantes inscritos em cada evento.
- Edição e remoção de inscrições; ao remover um participante, a vaga é devolvida.
- Preservação da quantidade de inscritos ao alterar o total de vagas de um evento, sem permitir que o novo total fique abaixo do número de inscritos.

## Tecnologias

- **Java 21**
- **Spring Boot 4.1.2**
- **Spring MVC** e **Thymeleaf** para as páginas web
- **Spring Data JPA / Hibernate** para persistência
- **Jakarta Validation** para validação dos dados de inscrição
- **PostgreSQL** como banco de dados
- **Supabase** para hospedagem do PostgreSQL
- **Render** para hospedagem da aplicação
- **Maven** para build e gerenciamento de dependências
- **Bootstrap 5** para estilização das páginas

## Estrutura do projeto

```
src/
├── main/
│   ├── java/com/exemplo/eventos/
│   │   ├── controller/   # Rotas e controle das páginas
│   │   ├── dto/          # Objeto de transferência e validação da inscrição
│   │   ├── model/        # Entidades Evento e Inscricao
│   │   ├── repository/   # Repositórios Spring Data JPA
│   │   ├── service/      # Regras de negócio
│   │   └── EventosApplication.java
│   └── resources/
│       ├── templates/    # Páginas Thymeleaf
│       └── application.properties
└── test/                 # Testes automatizados
```



## Estrutura do Banco de Dados

O sistema utiliza PostgreSQL, hospedado no Supabase, para armazenar os eventos e as inscrições dos participantes.

O banco de dados possui duas tabelas principais:

### Tabela `eventos`

Armazena as informações dos eventos cadastrados pelo administrador.

| **Campo**         | **Tipo**     | **Descrição**                 |
| ----------------- | ------------ | ----------------------------- |
| id                | SERIAL       | Identificador único do evento |
| titulo            | VARCHAR(150) | Nome do evento                |
| data_evento       | TIMESTAMP    | Data e horário do evento      |
| vagas_totais      | INT          | Quantidade total de vagas     |
| vagas_disponiveis | INT          | Quantidade de vagas restantes |

### Tabela `inscricoes`

Armazena os dados dos participantes inscritos nos eventos.

| **Campo**         | **Tipo**     | **Descrição**                       |
| ----------------- | ------------ | ----------------------------------- |
| id                | SERIAL       | Identificador único da inscrição    |
| evento_id         | INT          | Identificador do evento relacionado |
| nome_participante | VARCHAR(150) | Nome do participante                |
| email             | VARCHAR(150) | E-mail do participante              |
| data_inscricao    | TIMESTAMP    | Data e horário da inscrição         |

### Script SQL

```
CREATE TABLE eventos (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    data_evento TIMESTAMP NOT NULL,
    vagas_totais INT NOT NULL,
    vagas_disponiveis INT NOT NULL
);

CREATE TABLE inscricoes (
    id SERIAL PRIMARY KEY,
    evento_id INT NOT NULL,
    nome_participante VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    data_inscricao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evento
        FOREIGN KEY (evento_id)
        REFERENCES eventos(id)
        ON DELETE CASCADE
);
```

### Relacionamento entre as tabelas

A tabela `inscricoes` possui uma chave estrangeira (`evento_id`) que referencia a tabela `eventos`.

Dessa forma, um evento pode possuir várias inscrições, estabelecendo um relacionamento de um para muitos (1).

A restrição `ON DELETE CASCADE` garante que, ao excluir um evento, suas inscrições relacionadas também sejam excluídas automaticamente.



## Modelo de dados

A aplicação utiliza duas entidades principais:

- **Evento (`eventos`)**: título, data e horário, quantidade total de vagas e quantidade de vagas disponíveis.
- **Inscrição (`inscricoes`)**: evento relacionado, nome do participante, e-mail, data da inscrição e código único de cancelamento.

Cada inscrição pertence a um evento. A aplicação atualiza as vagas disponíveis ao criar ou remover uma inscrição.

## Como executar localmente

### Pré-requisitos

- JDK 21
- Git (opcional)
- Acesso a um banco PostgreSQL, por exemplo, um projeto Supabase

### 1. Configure as variáveis de ambiente

Na pasta do projeto (a mesma pasta do `pom.xml`), crie um arquivo `.env` para desenvolvimento local:

```
spring.datasource.url=jdbc:postgresql://SEU_HOST:5432/postgres
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
app.admin.codigo=DEFINA_UM_CODIGO_ADMINISTRATIVO
```

Substitua os valores de exemplo pelos dados de conexão do seu banco PostgreSQL e defina um código administrativo forte. Não compartilhe esse arquivo e não o envie ao Git.

O `application.properties` importa o `.env` local quando ele existe. Em ambientes hospedados, configure os valores como variáveis de ambiente no painel do serviço.

### 2. Execute a aplicação

No Windows (PowerShell ou Prompt de Comando):

```
mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```
./mvnw spring-boot:run
```

A aplicação utiliza a porta `8080` localmente. Acesse no navegador:

```
http://localhost:8080
```

## Rotas principais

As rotas abaixo são páginas e ações de uma aplicação web MVC (não uma API REST pública).

| **Método** | **Rota**                       | **Descrição**                                       | **Acesso** |
| ---------- | ------------------------------ | --------------------------------------------------- | ---------- |
| GET        | /                              | Lista os eventos e apresenta as opções de inscrição | Público    |
| POST       | /inscrever                     | Realiza uma inscrição                               | Público    |
| POST       | /cancelar-inscricao            | Cancela uma inscrição com e-mail e código           | Público    |
| GET        | /admin/login                   | Exibe a tela de acesso administrativo               | Público    |
| POST       | /admin/login                   | Valida o código administrativo                      | Público    |
| GET        | /admin/logout                  | Encerra a sessão administrativa                     | Admin      |
| GET        | /admin                         | Exibe o painel de eventos                           | Admin      |
| GET        | /admin/novo                    | Exibe o formulário de novo evento                   | Admin      |
| POST       | /admin/salvar                  | Cria ou atualiza um evento                          | Admin      |
| GET        | /admin/editar/{id}             | Exibe o formulário de edição                        | Admin      |
| GET        | /admin/deletar/{id}            | Exclui um evento                                    | Admin      |
| GET        | /admin/inscricoes/{id}         | Lista participantes de um evento                    | Admin      |
| POST       | /admin/inscricoes/{id}/editar  | Atualiza nome e e-mail de uma inscrição             | Admin      |
| POST       | /admin/inscricoes/{id}/excluir | Remove uma inscrição e devolve a vaga               | Admin      |

## Deploy no Render

1. Envie o código do projeto para um repositório Git (GitHub, por exemplo), sem incluir `.env`, credenciais ou arquivos gerados.
2. No Render, crie um **Web Service** conectado ao repositório.
3. Configure o ambiente para Java (Docker ou outra opção Java disponível na sua conta) e use os comandos Maven abaixo, considerando que a raiz do serviço seja a pasta que contém o `pom.xml`.
4. Configure as variáveis de ambiente no painel do Render:

| **Variável**               | **Valor**                                                                             |
| -------------------------- | ------------------------------------------------------------------------------------- |
| SPRING_DATASOURCE_URL      | URL JDBC do PostgreSQL no Supabase, no formato `jdbc:postgresql://HOST:5432/postgres` |
| SPRING_DATASOURCE_USERNAME | Usuário do banco Supabase                                                             |
| SPRING_DATASOURCE_PASSWORD | Senha do banco Supabase                                                               |
| APP_ADMIN_CODIGO           | Código definido para o acesso administrativo                                          |

5. Use os comandos de build e inicialização:

**Build command**

```
./mvnw clean package -DskipTests
```

**Start command**

```
java -jar target/eventos-0.0.1-SNAPSHOT.jar
```

A aplicação está configurada para utilizar a variável `PORT` fornecida pelo ambiente de hospedagem, com `8080` como valor padrão local.

> Observação: confirme que a raiz do serviço no Render aponta para a pasta `eventos/eventos` do repositório (a pasta onde está o `pom.xml`). Se você reorganizar o repositório, ajuste a raiz e os comandos de acordo.

## Banco de dados e configuração

O projeto utiliza PostgreSQL via JDBC e Spring Data JPA. A propriedade `spring.jpa.hibernate.ddl-auto=update` permite que o Hibernate atualize o esquema conforme as entidades da aplicação. Para produção, é recomendável planejar migrações versionadas e uma estratégia de backup antes de alterações no banco.

As credenciais devem ser mantidas em variáveis de ambiente ou em um arquivo `.env` local não versionado. Nunca coloque senhas, URLs com credenciais ou o código administrativo diretamente neste README ou no repositório.

## Testes

Para executar os testes automatizados:

```
./mvnw test
```

No Windows:

```
mvnw.cmd test
```



---



Desenvolvido com Java e Spring Boot.
