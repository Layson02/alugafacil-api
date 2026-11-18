# AlugaFácil API

API REST para gerenciamento de locação de mesas, cadeiras, ferramentas e
outros equipamentos.

## Objetivo

Organizar clientes, itens, reservas, retiradas e devoluções, evitando conflitos
de datas e reservas acima da disponibilidade.

## Tecnologias

- Java 21
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- Flyway
- SQL Server
- Maven

## Funcionalidades previstas

- Cadastro de clientes
- Cadastro e gerenciamento de itens
- Criação e consulta de reservas
- Validação de disponibilidade por período
- Registro de retiradas
- Registro de devoluções
- Testes automatizados

## Modelo de Dados (DER)

```mermaid
erDiagram
    CLIENTES ||--o{ RESERVAS : "realiza"
    RESERVAS ||--|{ RESERVA_ITENS : "contem"
    ITENS ||--o{ RESERVA_ITENS : "alocado_em"
    RESERVAS ||--o| RETIRADAS : "registra"
    RESERVAS ||--o| DEVOLUCOES : "finaliza"

    CLIENTES {
        bigint id PK
        varchar nome "NOT NULL"
        varchar documento "NOT NULL, UNIQUE"
        varchar telefone "NOT NULL"
        varchar email "NOT NULL, UNIQUE"
    }

    ITENS {
        bigint id PK
        varchar nome "NOT NULL"
        varchar descricao
        varchar categoria "NOT NULL"
        int quantidade_total "NOT NULL (> 0)"
        bit ativo "NOT NULL (DEFAULT 1)"
    }

    RESERVAS {
        bigint id PK
        bigint cliente_id FK "NOT NULL"
        date data_inicio "NOT NULL"
        date data_fim "NOT NULL"
        varchar status "PENDENTE | CONFIRMADA | RETIRADA | DEVOLVIDA | CANCELADA"
        datetime2 criado_em "NOT NULL"
    }

    RESERVA_ITENS {
        bigint id PK
        bigint reserva_id FK "NOT NULL"
        bigint item_id FK "NOT NULL"
        int quantidade "NOT NULL (> 0)"
    }

    RETIRADAS {
        bigint id PK
        bigint reserva_id FK "NOT NULL, UNIQUE"
        datetime2 data_retirada "NOT NULL"
        varchar observacao
    }

    DEVOLUCOES {
        bigint id PK
        bigint reserva_id FK "NOT NULL, UNIQUE"
        datetime2 data_devolucao "NOT NULL"
        varchar observacao
    }
```

## Pré-requisitos

- JDK 21
- SQL Server, quando a aplicação precisar acessar o banco
- Git

Verifique a instalação:

```powershell
java -version
.\mvnw.cmd -version
```

## Configuração por variáveis de ambiente

As credenciais e a conexão com o banco não devem ser gravadas no Git.
O Spring Boot reconhece automaticamente as variáveis
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e
`SPRING_DATASOURCE_PASSWORD`. O arquivo
[application-example.properties](src/main/resources/application-example.properties)
mostra a configuração equivalente em formato `.properties`.

### Windows PowerShell

Defina as variáveis na sessão atual do terminal:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:sqlserver://localhost:1433;databaseName=alugafacil;encrypt=true;trustServerCertificate=true"
$env:SPRING_DATASOURCE_USERNAME = "sa"
$env:SPRING_DATASOURCE_PASSWORD = "sua-senha-local"
```

Para verificar sem exibir a senha:

```powershell
Write-Output $env:SPRING_DATASOURCE_URL
Write-Output $env:SPRING_DATASOURCE_USERNAME
if ($env:SPRING_DATASOURCE_PASSWORD) { Write-Output "SPRING_DATASOURCE_PASSWORD configurada" }
```

Para tornar as variáveis permanentes no usuário do Windows, use o Painel de
Controle ou:

```powershell
[Environment]::SetEnvironmentVariable("SPRING_DATASOURCE_URL", "jdbc:sqlserver://localhost:1433;databaseName=alugafacil;encrypt=true;trustServerCertificate=true", "User")
[Environment]::SetEnvironmentVariable("SPRING_DATASOURCE_USERNAME", "sa", "User")
[Environment]::SetEnvironmentVariable("SPRING_DATASOURCE_PASSWORD", "sua-senha-local", "User")
```


### Linux/macOS

```bash
export SPRING_DATASOURCE_URL='jdbc:sqlserver://localhost:1433;databaseName=alugafacil;encrypt=true;trustServerCertificate=true'
export SPRING_DATASOURCE_USERNAME='sa'
export SPRING_DATASOURCE_PASSWORD='sua-senha-local'
```


## Executar a aplicação

Com as variáveis configuradas:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

## Executar os testes

Windows:

```powershell
.\mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

## Regra principal

Uma reserva deve ser recusada quando a quantidade solicitada ultrapassar a
disponibilidade do item no período informado.

## Organização do trabalho

O desenvolvimento é acompanhado por um quadro Kanban:

```text
Backlog -> In progress -> Done
```

As issues são classificadas como `Task`, `Feature` ou `Bug`.
