# Javerna

Projeto da disciplina de Programação para a Web 3.

O objetivo é modelar um sistema de expedições científicas em cavernas usando
Java, JPA e PostgreSQL.

Por enquanto, o repositório contém a configuração inicial do projeto e o
diagrama de classes. As entidades serão implementadas nas próximas etapas.

## Diagrama

O diagrama está no arquivo `doc/diagrama-de-classes.puml`.

## Tecnologias

- Java 17
- Jakarta Persistence 3.1
- Hibernate 6.4
- PostgreSQL
- Maven
- Lombok

## Banco de dados

A unidade de persistência `javernaPU` usa o banco `javerna`. O usuário e a senha
do PostgreSQL podem ser alterados no `persistence.xml`.

## Compilação

```bash
mvn clean compile
```
