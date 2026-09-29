package br.edu.ifpb.pweb3.javerna;

import br.edu.ifpb.pweb3.javerna.model.entity.*;
import br.edu.ifpb.pweb3.javerna.model.enums.*;
import br.edu.ifpb.pweb3.javerna.model.repository.ExpedicaoConsultaRepository;
import br.edu.ifpb.pweb3.javerna.persistence.TransacaoJta;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Hibernate;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static br.edu.ifpb.pweb3.javerna.DadosTeste.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConsultasIntegracaoTest {
    private EmbeddedPostgres postgres;
    private EntityManagerFactory factory;
    private TransacaoJta transacao;
    private DadosTeste.Ids ids;
    private final List<String> comandos = new ArrayList<>();
    private final List<String> evidencias = new ArrayList<>();

    @BeforeAll
    void preparar() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        factory = Persistence.createEntityManagerFactory("javernaPU", Map.of(
                "jakarta.persistence.jdbc.url", postgres.getJdbcUrl("postgres", "postgres"),
                "jakarta.persistence.jdbc.password", "",
                "jakarta.persistence.schema-generation.database.action", "drop-and-create",
                "hibernate.hbm2ddl.halt_on_error", "true",
                "hibernate.hbm2ddl.schema-generation.script.append", "false",
                "jakarta.persistence.schema-generation.scripts.action", "create",
                "jakarta.persistence.schema-generation.scripts.create-target", "target/schema-postgresql.sql",
                "hibernate.session_factory.statement_inspector", (StatementInspector) sql -> {
                    comandos.add(sql);
                    return sql;
                }));
        transacao = new TransacaoJta(factory);
        ids = transacao.executar(DadosTeste::criar);
    }

    @AfterAll
    void encerrar() throws Exception {
        try {
            Files.write(Path.of("target/consultas-verificadas.sql"), evidencias);
            if (factory != null) factory.close();
        } finally {
            if (postgres != null) postgres.close();
        }
    }

    @BeforeEach
    void limparSql() {
        comandos.clear();
    }

    @Test
    void listaSomenteCamposDoResumo() {
        transacao.executar(em -> {
            var resultados = new ExpedicaoConsultaRepository(em)
                    .listarPorPeriodoESituacao(INICIO, FIM, SituacaoExpedicao.PLANEJADA);
            assertFalse(resultados.isEmpty());
            assertEquals("Caverna de teste", resultados.get(0).caverna());
            return null;
        });
        assertEquals(1, comandos.size());
        assertFalse(comandos.get(0).contains("objetivo"));
        assertFalse(comandos.get(0).contains("orcamento"));
        assertSemBinarios();
        registrar("1 - Listagem de expedicoes: um SELECT com projecao");
    }

    @Test
    void detalhesIncluemPapeisSemDadosPessoaisOuBinarios() {
        var detalhes = transacao.executar(em -> new ExpedicaoConsultaRepository(em)
                .buscarDetalhesComParticipantes(ids.expedicao()).orElseThrow());
        assertEquals(2, detalhes.participantes().size());
        assertEquals(Set.of(PapelParticipante.GUIA, PapelParticipante.PESQUISADOR),
                new HashSet<>(detalhes.participantes().stream().map(p -> p.papel()).toList()));
        assertEquals(2, comandos.size());
        assertFalse(String.join(" ", comandos).contains("endereco"));
        assertFalse(String.join(" ", comandos).contains("cpf"));
        assertSemBinarios();
        registrar("2 - Detalhes: dois SELECTs, independentemente do numero de participantes");
    }

    @Test
    void coletasFazemFetchDoSetorEDoPesquisadorSemNMaisUm() {
        transacao.executar(em -> {
            var coletas = new ExpedicaoConsultaRepository(em).listarColetasDaExpedicao(ids.expedicao());
            assertEquals(2, coletas.size());
            for (ColetaCientifica coleta : coletas) {
                assertEquals("Galeria principal", coleta.getSetor().getDenominacao());
                assertEquals("Geologia", coleta.getPesquisadorResponsavel().getAreaPrincipalPesquisa());
                assertFalse(Hibernate.isInitialized(coleta.getAmostras()));
            }
            return null;
        });
        assertEquals(1, comandos.size());
        assertSemBinarios();
        registrar("3 - Coletas: um SELECT com JOIN FETCH; amostras nao inicializadas");
    }

    @Test
    void carregaAmostrasApenasNaConsultaDosDetalhesDaColeta() {
        transacao.executar(em -> {
            var amostras = new ExpedicaoConsultaRepository(em).listarAmostrasDaColeta(ids.coleta());
            assertEquals(1, amostras.size());
            assertEquals(new BigDecimal("12.3456"), amostras.get(0).getMassaOuVolume());
            assertArrayEquals(FOTO, amostras.get(0).getFotografia());
            assertFalse(Hibernate.isInitialized(amostras.get(0).getColeta()));
            return null;
        });
        assertEquals(1, comandos.size());
        registrar("4 - Amostras: consulta explicita ao abrir a coleta, incluindo sua fotografia");
    }

    @Test
    void baixaCadaArquivoEmUmSelectSeparado() {
        transacao.executar(em -> {
            var repository = new ExpedicaoConsultaRepository(em);
            assertArrayEquals(MAPA, repository.baixarMapaRota(ids.expedicao()));
            assertArrayEquals(AUTORIZACAO, repository.baixarAutorizacaoAssinada(ids.expedicao()));
            assertArrayEquals(RELATORIO, repository.baixarRelatorioFinal(ids.expedicao()));
            return null;
        });
        assertEquals(3, comandos.size());
        assertTrue(comandos.get(0).contains("mapa_rota"));
        assertFalse(comandos.get(0).contains("arquivo_assinado"));
        assertTrue(comandos.get(1).contains("arquivo_assinado"));
        assertTrue(comandos.get(2).contains("arquivo_completo"));
        assertTrue(comandos.stream().noneMatch(sql -> sql.contains(" join ")));
        registrar("6 - Downloads: um SELECT escalar para cada arquivo");
    }

    @Test
    void retornaVazioQuandoExpedicaoOuArquivoNaoExistem() {
        transacao.executar(em -> {
            var repository = new ExpedicaoConsultaRepository(em);
            assertTrue(repository.buscarDetalhesComParticipantes(-1L).isEmpty());
            assertNull(repository.baixarMapaRota(-1L));
            assertNull(repository.baixarAutorizacaoAssinada(-1L));
            assertNull(repository.baixarRelatorioFinal(-1L));
            return null;
        });
    }

    @Test
    void equipamentosConsideramDevolucaoRealSemCarregarHistorico() {
        List<Long> equipamentos = transacao.executar(em -> {
            List<Long> resultado = new ArrayList<>();
            Expedicao expedicao = em.find(Expedicao.class, ids.expedicao());
            for (int i = 0; i < 4; i++) {
                Equipamento equipamento = new Equipamento();
                equipamento.setCodigoPatrimonial("EQ-" + i);
                equipamento.setNome("Equipamento " + i);
                equipamento.setTipo(TipoEquipamento.MEDICAO);
                equipamento.setFabricante("Fabricante");
                equipamento.setValorAquisicao(new BigDecimal("500.00"));
                equipamento.setDataCompra(INICIO.toLocalDate().minusMonths(1));
                equipamento.setSituacaoOperacional(SituacaoOperacional.DISPONIVEL);
                em.persist(equipamento);
                resultado.add(equipamento.getId());
                if (i == 0) continue;
                UtilizacaoEquipamento utilizacao = new UtilizacaoEquipamento();
                utilizacao.setEquipamento(equipamento);
                utilizacao.setResponsavel(em.getReference(Pessoa.class, ids.pesquisador()));
                utilizacao.setDataHoraRetirada(INICIO.minusDays(1));
                utilizacao.setPrevisaoDevolucao(INICIO.plusDays(1));
                utilizacao.setEstadoSaida("Bom");
                if (i == 2) utilizacao.setDataHoraDevolucao(INICIO.plusHours(1));
                if (i == 3) utilizacao.setDataHoraDevolucao(INICIO.minusHours(1));
                expedicao.adicionarUtilizacaoEquipamento(utilizacao);
            }
            return resultado;
        });
        comandos.clear();
        transacao.executar(em -> {
            var disponiveis = new ExpedicaoConsultaRepository(em).listarEquipamentosDisponiveis(INICIO, FIM);
            assertEquals(Set.of(equipamentos.get(0), equipamentos.get(3)),
                    new HashSet<>(disponiveis.stream().map(Equipamento::getId).toList()));
            disponiveis.forEach(e -> assertFalse(Hibernate.isInitialized(e.getUtilizacoes())));
            return null;
        });
        assertEquals(1, comandos.size());
        registrar("5 - Equipamentos: um SELECT com NOT EXISTS e devolucao real ou prevista");
    }

    @Test
    void removeAmostraOrfaSemRemoverColeta() {
        var novos = transacao.executar(DadosTeste::criar);
        transacao.executar(em -> {
            ColetaCientifica coleta = em.find(ColetaCientifica.class, novos.coleta());
            coleta.removerAmostra(coleta.getAmostras().get(0));
            return null;
        });
        transacao.executar(em -> {
            assertNull(em.find(Amostra.class, novos.amostra()));
            assertNotNull(em.find(ColetaCientifica.class, novos.coleta()));
            return null;
        });
    }

    @Test
    void herancaJoinedPreservaTiposEIdentidade() {
        transacao.executar(em -> {
            assertInstanceOf(Pesquisador.class, em.find(Pessoa.class, ids.pesquisador()));
            assertInstanceOf(GuiaEspeleologia.class, em.find(Pessoa.class, ids.guia()));
            Number quantidade = (Number) em.createNativeQuery(
                    "select count(*) from pessoa p join pesquisador pe on pe.pessoa_id = p.id where p.id = :id")
                    .setParameter("id", ids.pesquisador()).getSingleResult();
            assertEquals(1, quantidade.intValue());
            return null;
        });
    }

    @Test
    void bancoImpedeParticipanteDuplicadoMesmoForaDoMetodoAuxiliar() {
        assertThrows(IllegalStateException.class, () -> transacao.executar(em -> {
            em.createNativeQuery("""
                    insert into participacao_expedicao
                    (expedicao_id, pessoa_id, papel, data_confirmacao, valor_diaria,
                     quantidade_prevista_dias, presenca_confirmada)
                    values (:expedicao, :pessoa, 'PESQUISADOR', current_date, 100, 1, true)
                    """)
                    .setParameter("expedicao", ids.expedicao())
                    .setParameter("pessoa", ids.pesquisador()).executeUpdate();
            return null;
        }));
        long quantidade = transacao.executar(em -> em.createQuery(
                        "select count(p) from ParticipacaoExpedicao p where p.expedicao.id = :id", Long.class)
                .setParameter("id", ids.expedicao()).getSingleResult());
        assertEquals(2, quantidade);
    }

    @Test
    void rejeitaExpedicaoComPeriodoInvertidoEReverteAlteracao() {
        assertThrows(IllegalStateException.class, () -> transacao.executar(em -> {
            em.find(Expedicao.class, ids.expedicao()).setTerminoPrevisto(INICIO.minusHours(1));
            return null;
        }));
        var fim = transacao.executar(em -> new ExpedicaoConsultaRepository(em)
                .buscarDetalhesComParticipantes(ids.expedicao()).orElseThrow().terminoPrevisto());
        assertEquals(FIM, fim);
    }

    @Test
    void remocaoDaExpedicaoRemoveDependentesMasPreservaCadastros() {
        var novos = transacao.executar(DadosTeste::criar);
        transacao.executar(em -> {
            em.remove(em.find(Expedicao.class, novos.expedicao()));
            return null;
        });
        transacao.executar(em -> {
            assertNull(em.find(PlanoSeguranca.class, novos.plano()));
            assertNull(em.find(ColetaCientifica.class, novos.coleta()));
            assertNull(em.find(Amostra.class, novos.amostra()));
            assertNotNull(em.find(Caverna.class, novos.caverna()));
            assertNotNull(em.find(Pessoa.class, novos.pesquisador()));
            assertNotNull(em.find(SetorPesquisa.class, novos.setor()));
            return null;
        });
    }

    @Test
    void coletaAceitaMedicoesAusentes() {
        var novos = transacao.executar(DadosTeste::criar);
        transacao.executar(em -> {
            ColetaCientifica coleta = em.find(ColetaCientifica.class, novos.coleta());
            coleta.setTemperatura(null);
            coleta.setUmidadeRelativa(null);
            coleta.setProfundidade(null);
            return null;
        });
        transacao.executar(em -> {
            ColetaCientifica coleta = em.find(ColetaCientifica.class, novos.coleta());
            assertNull(coleta.getTemperatura());
            assertNull(coleta.getUmidadeRelativa());
            assertNull(coleta.getProfundidade());
            return null;
        });
    }

    @Test
    void esquemaUsaBooleanOidPrecisaoEIdentificadorAutoincrementavel() {
        transacao.executar(em -> {
            assertEquals("boolean", em.createNativeQuery("""
                    select data_type from information_schema.columns
                    where table_schema = 'public' and table_name = 'amostra' and column_name = 'material_perigoso'
                    """).getSingleResult());
            assertEquals("oid", em.createNativeQuery("""
                    select udt_name from information_schema.columns
                    where table_schema = 'public' and table_name = 'amostra' and column_name = 'fotografia'
                    """).getSingleResult());
            Object[] precisao = (Object[]) em.createNativeQuery("""
                    select numeric_precision, numeric_scale from information_schema.columns
                    where table_schema = 'public' and table_name = 'amostra' and column_name = 'massa_ou_volume'
                    """).getSingleResult();
            assertEquals(12, ((Number) precisao[0]).intValue());
            assertEquals(4, ((Number) precisao[1]).intValue());
            String geracao = (String) em.createNativeQuery("""
                    select column_default from information_schema.columns
                    where table_schema = 'public' and table_name = 'caverna' and column_name = 'id'
                    """).getSingleResult();
            assertTrue(geracao.startsWith("nextval("));
            return null;
        });
    }

    private void assertSemBinarios() {
        String sql = String.join(" ", comandos);
        for (String campo : List.of("mapa_rota", "arquivo_assinado", "arquivo_completo", "fotografia")) {
            assertFalse(sql.contains(campo), "Consulta carregou " + campo);
        }
    }

    private void registrar(String caso) {
        evidencias.add("-- " + caso);
        comandos.forEach(sql -> evidencias.add(FormatStyle.BASIC.getFormatter().format(sql) + ";"));
        evidencias.add("");
    }
}
