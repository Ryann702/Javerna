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

Na verificação desta entrega, `mvn clean verify` concluiu com 20 testes, sem
falhas ou erros, usando PostgreSQL 14.15 temporário.

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
