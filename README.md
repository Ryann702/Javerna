# Javerna

Projeto desenvolvido na disciplina de Programação para a Web 3 do curso de
Engenharia de Software do IFPB.

O Javerna representa o domínio de expedições científicas em cavernas. O modelo
inclui cavernas, setores de pesquisa, participantes, equipamentos, coletas e
amostras.

Modelo persistente e consultas JPA para o projeto de expedições científicas
subterrâneas, com transações JTA gerenciadas pelo Narayana.

## Tecnologias

- Java 17
- Jakarta Persistence 3.1
- Hibernate 6.4
- PostgreSQL
- Maven
- Lombok
- Jakarta Transactions 2.0.1 e Narayana 7.2.2

## Estrutura

- `doc/diagrama-de-classes.puml`: diagrama de classes do domínio;
- `src/main/resources/META-INF/persistence.xml`: configuração da unidade de
  persistência;
- `pom.xml`: dependências e configuração do projeto Maven.
- `src/main/java/br/edu/ifpb/pweb3/javerna/model`: entidades, tipos incorporáveis,
  enumerações, resultados das consultas e repositório;
- `src/main/java/br/edu/ifpb/pweb3/javerna/persistence`: transações e conexão JTA;
- `src/main/resources/META-INF/orm.xml`: consultas nomeadas;
- `doc/relatorio-tecnico.md`: justificativas do mapeamento e das consultas.

## Banco de dados

A unidade de persistência `javernaPU` está configurada para o banco `javerna`,
na porta `5432`. As credenciais podem ser alteradas no arquivo
`persistence.xml`. Crie o banco antes de executar a aplicação:

```sql
CREATE DATABASE javerna;
```

O Hibernate cria e atualiza as tabelas com `hibernate.hbm2ddl.auto=update`.

A conexão usa `PGXADataSource` e o driver transacional do Narayana. As operações
ficam dentro de `TransacaoJta.executar`, que abre a transação, associa o
`EntityManager`, confirma ou desfaz as alterações e fecha o contexto.

## Compilação

Com Java 17 e Maven instalados, execute:

```bash
mvn clean compile
```

Para listar expedições inteiramente contidas em um período:

```bash
mvn compile exec:java -Dexec.args="2026-01-01T00:00 2026-12-31T23:59 PLANEJADA"
```

Essa aplicação aceita as variáveis `JAVERNA_DB_URL`, `JAVERNA_DB_USER` e
`JAVERNA_DB_PASSWORD` para substituir as credenciais locais do `persistence.xml`.

## Testes

```bash
mvn clean verify
```

Os testes iniciam um PostgreSQL temporário em uma porta livre e encerram o banco
ao terminar. Não precisam de Docker nem usam o banco `javerna`. A primeira
execução baixa os binários do PostgreSQL pelo Maven.

São verificados commit e rollback JTA, as seis consultas, quantidade de SELECTs,
ausência de binários nas listagens, herança, cascatas, remoção de órfãos e
restrições de integridade. Os resultados ficam em `target/surefire-reports/`;
o esquema e o SQL capturado são regenerados em `target/schema-postgresql.sql`
e `target/consultas-verificadas.sql`.
