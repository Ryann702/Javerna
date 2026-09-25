# Javerna

Projeto da disciplina de Programação para a Web 3 para modelar expedições
científicas em cavernas usando Java, Jakarta Persistence (JPA) e PostgreSQL.

## Etapa atual

A modelagem inicial do domínio e as decisões de mapeamento objeto-relacional
estão concluídas. Esta etapa contém:

- levantamento das classes a partir do enunciado;
- diagrama de classes com atributos, herança e relacionamentos;
- definição de ownership, cascatas, remoção de órfãos e fetch;
- escolha da estratégia de herança `JOINED` para `Pessoa`;
- configuração inicial do Maven e da unidade de persistência.

O diagrama está em `doc/diagrama-de-classes.puml` e as decisões técnicas estão
registradas em `doc/decisoes-orm.md`.

A próxima etapa é implementar os tipos incorporáveis e as enumerações do
domínio.

## Tecnologias

- Java 17
- Jakarta Persistence 3.1
- Hibernate 6.4
- PostgreSQL
- Maven
- Lombok

## Banco de dados

A unidade de persistência `javernaPU` está configurada para o banco `javerna`
na porta padrão do PostgreSQL. Antes de executar o projeto, o banco deve ser
criado e os dados de usuário e senha podem ser ajustados no arquivo
`persistence.xml`.

## Compilação

```bash
mvn clean compile
```
