# Sistema de Gerenciamento de Eventos

Aplicação web desenvolvida em Java com Spring Boot para gerenciamento de eventos e inscrições de participantes.

O sistema permite que visitantes visualizem eventos disponíveis e realizem inscrições informando nome e e-mail. O administrador possui acesso a um painel para cadastrar, editar e excluir eventos, além de gerenciar os participantes inscritos.

Os dados são armazenados em um banco de dados PostgreSQL hospedado no Supabase, enquanto a aplicação está hospedada na plataforma Render.

---

## Funcionalidades

### Área do Participante

- Visualização dos eventos disponíveis.
- Consulta da data e horário dos eventos.
- Visualização da quantidade de vagas disponíveis.
- Inscrição em eventos utilizando nome e e-mail.
- Cancelamento de inscrição utilizando e-mail e código de cancelamento.
- Atualização automática das vagas disponíveis após inscrições e cancelamentos.

### Área Administrativa

- Acesso protegido por código administrativo.
- Cadastro de novos eventos.
- Edição das informações dos eventos.
- Exclusão de eventos.
- Definição da data e horário dos eventos.
- Definição da quantidade total de vagas.
- Visualização dos participantes inscritos.
- Edição e exclusão de inscrições.
- Controle de vagas disponíveis.

---

## Tecnologias Utilizadas

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem de programação |
| Spring Boot | Framework principal |
| Spring MVC | Gerenciamento das requisições web |
| Spring Data JPA | Persistência de dados |
| Hibernate | Mapeamento objeto-relacional |
| Thymeleaf | Renderização das páginas HTML |
| PostgreSQL | Banco de dados relacional |
| Supabase | Hospedagem do banco de dados |
| Render | Hospedagem da aplicação |
| Maven | Gerenciamento de dependências |
| Bootstrap 5 | Estilização da interface |

---

## Estrutura do Projeto

A aplicação está organizada em camadas, separando as responsabilidades de controle, regras de negócio e acesso aos dados.

```text
src/
├── main/
│   ├── java/
│   │   └── com/exemplo/eventos/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       └── EventosApplication.java
│   │
│   └── resources/
│       ├── templates/
│       └── application.properties/
│
└── test/
