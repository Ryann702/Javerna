# Javerna

Projeto desenvolvido na disciplina de Programação para a Web 3 do curso de
Engenharia de Software do IFPB.

O Javerna representa o domínio de expedições científicas em cavernas. O modelo
inclui cavernas, setores de pesquisa, participantes, equipamentos, coletas e
amostras.

O projeto está na etapa de modelagem do domínio e configuração da persistência.

## Tecnologias

- Java 17
- Jakarta Persistence 3.1
- Hibernate 6.4
- PostgreSQL
- Maven
- Lombok

## Estrutura

- `doc/diagrama-de-classes.puml`: diagrama de classes do domínio;
- `src/main/resources/META-INF/persistence.xml`: configuração da unidade de
  persistência;
- `pom.xml`: dependências e configuração do projeto Maven.

## Banco de dados

A unidade de persistência `javernaPU` está configurada para o banco `javerna`,
na porta `5432`. As credenciais podem ser alteradas no arquivo
`persistence.xml`.

## Compilação

Com Java 17 e Maven instalados, execute:

```bash
mvn clean compile
```
