# Relatório técnico - Javerna

## Modelo e tipos

O modelo possui 14 entidades. `ParticipacaoExpedicao` e `UtilizacaoEquipamento`
representam associações com atributos próprios. A participação registra o papel
da pessoa na expedição; a utilização registra retirada, devolução e responsável
pelo equipamento. A combinação de pessoa e expedição possui restrição única.

`Endereco` e `Localizacao` são incorporáveis: seus campos ficam nas tabelas de
pessoa e caverna, sem identidade ou tabela independente. Os identificadores usam
`GenerationType.IDENTITY`, traduzido pelo Hibernate para geração automática no
PostgreSQL. Nas subclasses de pessoa, a chave é herdada.

Os enums usam `EnumType.STRING`; booleanos são colunas `boolean`; valores
monetários e medições usam `BigDecimal` com precisão e escala explícitas.
`LocalDate` representa datas sem horário e `LocalDateTime`, horários civis das
expedições e coletas. O modelo não realiza conversão de fuso horário.

Temperatura, umidade e profundidade da coleta podem ficar ausentes quando a
medição ainda não foi obtida. Observações, complemento do endereço e informações
de devolução ainda não ocorrida também são opcionais. Códigos, nomes e referências
essenciais possuem restrições de nulidade, tamanho e unicidade conforme o domínio.

## Herança

`Pessoa` é abstrata e usa `JOINED`. Os atributos comuns ficam em `pessoa`, e os
específicos em `pesquisador` e `guia_espeleologia`. A coluna `pessoa_id` das
subclasses é simultaneamente chave primária e estrangeira.

A escolha permite consultas polimórficas e novos tipos de pessoa sem adicionar
campos opcionais à tabela comum. O custo é a junção entre tabelas ao materializar
uma pessoa especializada. Nas consultas de participantes que precisam apenas de
nome e papel, uma projeção evita carregar os atributos das especializações.

## Associações e ciclo de vida

| Associação | Proprietário no mapeamento | Cascata e remoção |
| --- | --- | --- |
| Caverna / setores | `SetorPesquisa.caverna` | `ALL` e `orphanRemoval` na coleção de setores |
| Caverna / expedições | `Expedicao.caverna` | Sem remoção em cascata de expedições |
| Expedição / setores | `Expedicao.setores`, pela tabela `expedicao_setor` | Sem cascata para o cadastro de setores |
| Expedição / plano, autorização, relatório | A referência `expedicao` de cada documento | `ALL` e `orphanRemoval` no lado da expedição |
| Expedição / participações | `ParticipacaoExpedicao.expedicao` | `ALL` e `orphanRemoval` na expedição |
| Expedição / utilizações | `UtilizacaoEquipamento.expedicao` | `ALL` e `orphanRemoval` na expedição |
| Expedição / coletas | `ColetaCientifica.expedicao` | `ALL` e `orphanRemoval` na expedição |
| Coleta / amostras | `Amostra.coleta` | `ALL` e `orphanRemoval` na coleta |

As referências a pessoa, pesquisador, equipamento e setor não propagam exclusão
para esses cadastros. As coleções inversas usam `mappedBy`. Métodos de inclusão
e remoção atualizam os dois lados das associações bidirecionais.

O plano é obrigatório e exclusivo da expedição; a aplicação verifica sua
presença antes da persistência e a FK do plano é não nula e única. Autorização e
relatório são opcionais, mas exclusivos. O modelo mantém uma autorização
cadastrada por expedição, sem histórico de versões de autorização.

A inclusão de setores e coletas exige que o setor pertença à caverna da
expedição. A participação duplicada é rejeitada pelo método auxiliar e pela
restrição única no PostgreSQL. A utilização exige um responsável. Também são
validados o limite de participantes, períodos invertidos e custos negativos da
expedição e da utilização.

## Transações

A unidade de persistência usa `JTA`, a API Jakarta Transactions 2.0.1 e Narayana
7.2.2.Final. A demarcação segue `UserTransaction.begin()`, `commit()` e
`rollback()`, com fechamento do `EntityManager` ao terminar a operação.

O `JBossStandAloneJtaPlatform` integra Hibernate e Narayana. O
`ConexaoJtaProvider` fornece conexões obtidas de `PGXADataSource` pelo
`TransactionalDriver`, para que o banco participe da transação. Configurar
somente a plataforma JTA com o driver JDBC comum não foi suficiente: o teste
inicial mostrou que um INSERT permanecia após o rollback. Com o recurso
registrado no Narayana, o rollback desfez tanto SQL nativo quanto alterações
enviadas com `persist` e `flush`.

Cada chamada a `TransacaoJta.executar` cria seu próprio contexto. Transações
aninhadas não são aceitas. Ao ocorrer uma falha, a operação é revertida e o
contexto é encerrado; entidades gerenciadas nesse contexto não devem ser
reutilizadas como se ainda estivessem associadas a um `EntityManager`.

## Plano de buscas

As coleções e referências são declaradas `LAZY`. Nas consultas de leitura,
selecionamos os dados necessários a cada caso. `LAZY` em associações 1:1 inversas
não é tratado como garantia de que a entidade nunca será carregada.

Os binários usam `@Lob`, mapeado para `oid` no PostgreSQL. Não usamos bytecode
enhancement nem dependemos de `@Basic(LAZY)`. Ao materializar uma entidade que
contém um binário, esse campo pode ser lido junto. Por isso, as consultas de
listagem e detalhe usam projeções JPA (`SELECT new`) em vez de materializar os
documentos e o grafo completo da expedição. Os records de resultado não são
entidades e não precisam de mapeamento próprio.

| Caso | Estratégia | SELECTs do Hibernate verificados |
| --- | --- | --- |
| Listagem de expedições | Projeção de código, título, nome da caverna, datas e situação | 1 |
| Detalhes e participantes | Projeção da expedição e projeção de pessoa/nome/papel em consultas separadas | 2 |
| Coletas da expedição | `JOIN FETCH` para setor e pesquisador; amostras permanecem não inicializadas | 1 |
| Amostras da coleta | Consulta explícita por ID da coleta, com fotografia | 1 |
| Equipamentos disponíveis | `NOT EXISTS` sobre utilizações conflitantes, sem materializar o histórico | 1 |
| Downloads | Seleção escalar do mapa, autorização ou relatório | 1 por arquivo |

Nos detalhes, as duas consultas são fixas: não se executa uma consulta adicional
por participante. Os resultados podem ser usados após o fechamento do contexto.
Nas consultas que retornam entidades, somente as associações carregadas pelo
fetch join devem ser acessadas depois de fechar o contexto.

O período da listagem considera expedições inteiramente contidas no intervalo,
com limites inclusivos. A consulta de equipamentos mantém a exigência da situação
operacional atual `DISPONIVEL` e exclui utilizações que se sobrepõem ao período.
O fim de uma utilização é a devolução efetiva quando informada; caso contrário,
usa-se a previsão. Os limites também são inclusivos. Essa consulta representa
disponibilidade pelo cadastro atual e pelas datas registradas, não um histórico
de mudanças da situação operacional nem uma garantia de devolução futura.

## Problema N+1 e solução no projeto

O problema N+1 ocorre quando uma consulta busca uma lista de entidades e o acesso
aos seus relacionamentos provoca consultas adicionais para cada resultado.
Por exemplo, ao listar coletas e depois acessar o setor e o pesquisador de cada
uma, o Hibernate pode executar uma consulta inicial e outras para carregar as
referências. A quantidade depende das referências distintas e do que já está
carregado no contexto; com dois relacionamentos, pode ultrapassar 1 + N.

Uma consulta que permitiria esse problema seria:

```java
var coletas = em.createQuery("""
        SELECT c FROM ColetaCientifica c
        WHERE c.expedicao.id = :expedicaoId
        """, ColetaCientifica.class)
        .setParameter("expedicaoId", expedicaoId)
        .getResultList();

for (ColetaCientifica coleta : coletas) {
    System.out.println(coleta.getSetor().getDenominacao());
    System.out.println(coleta.getPesquisadorResponsavel().getNome());
}
```

Nesse exemplo, executado com o contexto aberto, os getters podem disparar novas
buscas. Declarar `LAZY` apenas adia essas buscas; não elimina N+1. Trocar tudo
para `EAGER` também não garante uma única consulta e pode carregar dados sem uso.

A solução adotada na consulta `ColetaCientifica.listarPorExpedicao`, registrada
em `src/main/resources/META-INF/orm.xml`, é buscar os dois relacionamentos
necessários com `JOIN FETCH`:

```jpql
SELECT c FROM ColetaCientifica c
JOIN FETCH c.setor
JOIN FETCH c.pesquisadorResponsavel
WHERE c.expedicao.id = :expedicaoId
ORDER BY c.dataHora
```

Assim, coletas, setores e pesquisadores são obtidos no mesmo SELECT. Um `JOIN`
comum permite filtrar ou projetar dados relacionados, mas não garante que a
associação da entidade retornada seja inicializada; o `FETCH` solicita esse
carregamento. Como setor e pesquisador são obrigatórios, usamos junções internas.
Para uma associação opcional que precisasse ser carregada mantendo os registros
sem correspondência, a alternativa seria `LEFT JOIN FETCH`.

Nas listagens de expedições, a solução é a projeção em `ExpedicaoResumo`: a
consulta seleciona diretamente os campos exibidos, incluindo o nome da caverna.
Nos detalhes de uma expedição, duas consultas retornam os dados da expedição e
todos os resumos dos participantes. Não há uma consulta por participante.
Equipamentos usam `NOT EXISTS` para verificar conflitos no banco sem percorrer
o histórico em Java. Amostras e arquivos binários são consultados separadamente
quando solicitados, evitando carregar todo o grafo na listagem inicial.

Essas soluções são específicas de cada caso de uso. Se uma nova tela listar
várias coletas com suas amostras, chamar `listarAmostrasDaColeta` dentro de um
laço voltaria a criar N+1. Nesse caso, buscaríamos as amostras de todas as coletas
selecionadas em uma consulta com `WHERE a.coleta.id IN :ids`, agrupando os
resultados por coleta. Também evitaríamos buscar várias coleções simultaneamente
com fetch join, pois isso pode multiplicar as linhas do resultado.

O teste `coletasFazemFetchDoSetorEDoPesquisadorSemNMaisUm` acessa setor e
pesquisador das coletas retornadas e exige apenas um SELECT, mantendo as amostras
não inicializadas. Os demais testes verificam as contagens da tabela anterior.
Essa verificação cobre as consultas implementadas, não qualquer acesso futuro
aos relacionamentos do modelo.

## Como executar o projeto

É necessário ter JDK 17 e Maven disponíveis no terminal. Para executar a
aplicação, também é necessário um PostgreSQL ativo. Os comandos abaixo devem
ser executados na raiz do projeto, onde está o `pom.xml`.

```powershell
java -version
mvn -version
```

Se um comando não for reconhecido, configure o diretório `bin` da instalação
correspondente no `PATH` e reabra o terminal. O projeto é uma aplicação de
console; não inicia uma página web.

### Preparar o banco e compilar

Crie o banco uma única vez pelo pgAdmin ou por um cliente SQL, conectado a um
banco existente, como `postgres`:

```sql
CREATE DATABASE javerna;
```

A configuração local fica em `src/main/resources/META-INF/persistence.xml`.
Por padrão, usa `jdbc:postgresql://localhost:5432/javerna`, usuário `postgres`
e senha `postgres`. Ajuste os valores conforme a instalação. A classe
`ConsultaExpedicoes` também aceita `JAVERNA_DB_URL`, `JAVERNA_DB_USER` e
`JAVERNA_DB_PASSWORD` como variáveis de ambiente para substituir esses valores.
O Hibernate cria ou atualiza as tabelas ao iniciar a persistência, com
`hibernate.hbm2ddl.auto=update`.

```powershell
mvn clean compile
```

Esse comando limpa os arquivos gerados e compila o código. Ele não executa a
carga nem as consultas.

### Povoar e consultar

Para executar a carga demonstrativa no PostgreSQL configurado:

```powershell
mvn compile exec:java "-Dexec.args=--povoar"
```

A carga ampliada contempla três cavernas, seis setores, três pesquisadores,
dois guias, seis equipamentos, cinco expedições, seis coletas e seis amostras.
Há cavernas sem expedições, pesquisador sem coletas e setores sem coletas,
permitindo demonstrar consultas com `LEFT JOIN`. Com as cinco expedições DEMO
já presentes, uma nova execução informa que nenhum dado foi alterado.

Para listar expedições planejadas inteiramente contidas em um intervalo:

```powershell
mvn compile exec:java "-Dexec.args=2026-01-01T00:00 2026-12-31T23:59 PLANEJADA"
```

Os três argumentos são início, fim e situação. As situações aceitas são
`PLANEJADA`, `AUTORIZADA`, `EM_ANDAMENTO`, `CONCLUIDA` e `CANCELADA`.
No PowerShell, mantenha todo o argumento `-Dexec.args=...` entre aspas.
O terminal exibe o SQL, os resumos encontrados e a quantidade de expedições.

O Maven executa `ConsultaExpedicoes.main`, configurado no `pom.xml`. Essa classe
abre a persistência e chama `ExpedicaoConsultaRepository` dentro de
`TransacaoJta`. O repositório usa `createNamedQuery` para localizar a consulta
no `orm.xml`, fornece os parâmetros e devolve os resultados. O XML não é
executado diretamente.

### Executar as verificações

Para compilar, executar todos os testes e gerar o JAR:

```powershell
mvn clean verify
```

Para executar somente a classe de testes que cobre as consultas e outras regras
de persistência:

```powershell
mvn "-Dtest=ConsultasIntegracaoTest" test
```

Para verificar especificamente a solução de N+1 nas coletas:

```powershell
mvn "-Dtest=ConsultasIntegracaoTest#coletasFazemFetchDoSetorEDoPesquisadorSemNMaisUm" test
```

Os testes usam PostgreSQL temporário com dados próprios; não consultam nem
alteram o banco local povoado. A primeira execução precisa baixar dependências
e os binários do banco. O resultado esperado é `BUILD SUCCESS`, sem falhas ou
erros nos testes. Os relatórios ficam em `target/surefire-reports/` e as
evidências SQL em `target/consultas-verificadas.sql`.

## Verificação

`mvn clean verify` inicia um PostgreSQL temporário e executa testes de integração
com o mesmo `persistence.xml`, substituindo apenas a conexão e as opções de
geração de esquema. Cada consulta medida usa um novo contexto para que o cache
do `EntityManager` não esconda SELECTs adicionais.

Os testes conferem o conteúdo dos resultados, a quantidade e os campos dos
SELECTs, download dos bytes, precisão das medições, herança polimórfica,
participação única, cascatas, remoção de órfãos e commit/rollback. O teste de
equipamentos inclui utilização aberta, devolução com sobreposição e devolução
anterior ao período consultado.

Após incluir a carga inicial, `mvn verify` concluiu com 21 testes, sem
falhas ou erros, usando PostgreSQL 14.15 temporário.

A carga demonstrativa é executada explicitamente pelo argumento `--povoar`.
Ela grava dados fictícios em uma única transação JTA e não modifica registros
quando as cinco expedições de demonstração já existem. O teste da carga verifica
as seis consultas, repetição sem duplicação, preservação de dados existentes e
rollback integral em caso de falha. Os anexos são PDFs demonstrativos, sem
validade documental; as fotografias das amostras permanecem opcionais e ausentes.

Os testes geram o esquema em `target/schema-postgresql.sql` e o SQL capturado em
`target/consultas-verificadas.sql`. Os pontos de interrogação representam os
parâmetros enviados pelo Hibernate. Esses arquivos podem ser usados na
demonstração e são regenerados localmente, sem versionamento no repositório.

## Referências

- Enunciado do Projeto TurmalinaPB, seções 8 a 11.
- Slides JPA Entity Manager, páginas 57 a 61: JTA, Narayana e tratamento de exceções.
- Slides JPA Fetch Plan: carregamento sob demanda, N+1 e fetch join.
- Slides JPA Relacionamentos e JPA Herança: ownership, cascatas e estratégias de herança.
- [Narayana - integração JDBC e recursos XA](https://www.narayana.io/docs/project/index.html).
