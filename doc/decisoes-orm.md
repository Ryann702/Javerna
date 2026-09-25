# Decisões de mapeamento objeto-relacional

Este documento registra as decisões tomadas antes da implementação das
entidades do projeto Javerna. O objetivo é manter o modelo Java, o esquema
PostgreSQL e as consultas JPA coerentes entre si.

## Identidade e tipos básicos

- Todas as entidades terão identificador `Long` gerado com
  `GenerationType.IDENTITY`, estratégia compatível com colunas identity do
  PostgreSQL.
- Campos essenciais serão `nullable = false` e textos terão limites definidos
  com `length`.
- Valores monetários e medições usarão `BigDecimal`, com `precision` e `scale`
  adequadas a cada atributo.
- Valores lógicos serão persistidos como `boolean`, sem conversores para texto.
- Enumerações serão persistidas com `EnumType.STRING`, evitando dependência da
  posição dos elementos.
- Datas civis usarão `LocalDate`, o agendamento local das expedições usará
  `LocalDateTime` e os eventos operacionais usarão `Instant`, normalizado em
  UTC.
- Arquivos e fotografias usarão `byte[]` e mapeamento de objeto grande. O
  carregamento tardio será verificado pelo SQL gerado, pois `@Basic(fetch =
  LAZY)` é uma indicação ao provedor.

## Objetos incorporáveis

`Endereco` e `Localizacao` serão mapeados com `@Embeddable` e usados por meio
de `@Embedded`. Eles não possuem identidade própria nem tabela exclusiva:
suas colunas ficam, respectivamente, nas tabelas de pessoa e caverna.

## Herança de pessoas

A hierarquia formada por `Pessoa`, `Pesquisador` e `GuiaEspeleologia` usará
`InheritanceType.JOINED`.

Essa estratégia foi escolhida porque:

- mantém os dados comuns em uma única tabela de pessoas;
- permite restrições de nulidade próprias nas tabelas das subclasses;
- admite associações polimórficas com `Pessoa`;
- facilita a inclusão futura de novos tipos de pessoa;
- evita as várias colunas nulas que seriam necessárias em `SINGLE_TABLE`.

O custo aceito é a necessidade de junções para materializar uma pessoa
especializada.

## Associações, ownership e ciclo de vida

| Associação | Lado proprietário | Cascata e remoção | Fetch planejado |
| --- | --- | --- | --- |
| Caverna - setores | `SetorPesquisa`, pela chave estrangeira | `ALL` e `orphanRemoval` a partir de `Caverna` | `LAZY` |
| Caverna - expedições | `Expedicao`, pela chave estrangeira | Sem remoção em cascata | `LAZY` |
| Expedição - setores | `Expedicao`, pela tabela de junção | Sem `REMOVE` | `LAZY` |
| Expedição - plano de segurança | `PlanoSeguranca`, com chave estrangeira única | `ALL` e `orphanRemoval` a partir de `Expedicao` | `LAZY` |
| Expedição - autorização | `AutorizacaoAmbiental`, com chave estrangeira única | `ALL` e `orphanRemoval` a partir de `Expedicao` | `LAZY` |
| Expedição - relatório final | `RelatorioFinal`, com chave estrangeira única | `ALL` e `orphanRemoval` a partir de `Expedicao` | `LAZY` |
| Expedição/Pessoa - participação | `ParticipacaoExpedicao`, pelas duas chaves estrangeiras | Ciclo de vida controlado pela expedição; sem cascata para pessoa | `LAZY` |
| Expedição/Equipamento/Pessoa - utilização | `UtilizacaoEquipamento`, pelas três chaves estrangeiras | Sem cascata para equipamento ou pessoa | `LAZY` |
| Expedição - coletas | `ColetaCientifica`, pela chave estrangeira | `ALL` e `orphanRemoval` a partir de `Expedicao` | `LAZY` |
| Coleta - amostras | `Amostra`, pela chave estrangeira | `ALL` e `orphanRemoval` a partir de `ColetaCientifica` | `LAZY` |

As associações bidirecionais terão métodos auxiliares para atualizar os dois
lados. Cascata de remoção não será aplicada a entidades independentes como
`Pessoa`, `Equipamento`, `Caverna` e `SetorPesquisa`.

## Restrições de integridade

Serão criadas restrições únicas para:

- CPF da pessoa;
- código ambiental da caverna;
- código da expedição;
- código patrimonial do equipamento;
- código de campo da amostra;
- combinação de pessoa e expedição em `ParticipacaoExpedicao`.

O domínio também verificará que todo setor associado a uma expedição pertence
à caverna dessa expedição. A expedição deverá ser criada com exatamente um
plano de segurança.

## Estratégia de carregamento

Coleções e associações para uma entidade serão mapeadas explicitamente como
`LAZY`. Cada caso de uso definirá seu plano de busca por projeção ou `JOIN
FETCH`, em vez de tornar o grafo inteiro `EAGER`.

As consultas obrigatórias serão implementadas da seguinte forma:

1. projeção resumida para listar expedições por período e situação;
2. `JOIN FETCH` dos participantes e seus papéis nos detalhes da expedição;
3. `JOIN FETCH` do setor e do pesquisador ao listar coletas;
4. consulta das amostras somente ao abrir os detalhes de uma coleta;
5. `NOT EXISTS` para equipamentos sem utilização sobreposta ao período pedido;
6. projeções de `byte[]` separadas para mapa, autorização e relatório final.

O SQL produzido pelo Hibernate será registrado durante a validação para
demonstrar a ausência de carregamento indiscriminado e do problema N+1.

## Convenções das entidades

- Entidades terão construtor sem argumentos para a JPA.
- Lombok poderá gerar getters e setters, mas não será usado `@Data` nas
  entidades para evitar recursão e inicialização acidental de associações lazy.
- `toString`, `equals` e `hashCode` não incluirão associações.
- Coleções serão inicializadas na declaração e expostas por interfaces como
  `List` ou `Set`.
