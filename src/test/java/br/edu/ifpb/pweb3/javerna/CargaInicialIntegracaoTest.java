package br.edu.ifpb.pweb3.javerna;

import br.edu.ifpb.pweb3.javerna.model.entity.Caverna;
import br.edu.ifpb.pweb3.javerna.model.entity.Expedicao;
import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoExpedicao;
import br.edu.ifpb.pweb3.javerna.model.repository.ExpedicaoConsultaRepository;
import br.edu.ifpb.pweb3.javerna.persistence.TransacaoJta;
import br.edu.ifpb.pweb3.javerna.seed.CargaInicial;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CargaInicialIntegracaoTest {
    @Test
    void cargaAtendeConsultasNaoDuplicaEPreservaDadosExistentes() throws Exception {
        try (var postgres = EmbeddedPostgres.builder().start()) {
            var factory = Persistence.createEntityManagerFactory("javernaPU", Map.of(
                    "jakarta.persistence.jdbc.url", postgres.getJdbcUrl("postgres", "postgres"),
                    "jakarta.persistence.jdbc.password", "", "hibernate.hbm2ddl.auto", "create-drop"));
            try {
                var tx = new TransacaoJta(factory);
                tx.executar(em -> { em.persist(DadosTeste.caverna("OUTRA-CAVERNA")); return null; });
                assertThrows(IllegalStateException.class, () -> tx.executar(em -> {
                    CargaInicial.povoar(em);
                    throw new IllegalArgumentException("Falha depois de criar os dados");
                }));
                long aposRollback = tx.executar(em -> em.createQuery("select count(e) from Expedicao e", Long.class).getSingleResult());
                assertEquals(0, aposRollback);
                assertTrue(tx.executar(CargaInicial::povoar));
                tx.executar(em -> {
                    var repo = new ExpedicaoConsultaRepository(em);
                    var inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
                    var fim = LocalDateTime.of(2026, 12, 31, 23, 59);
                    assertEquals("DEMO-EXP-002", repo.listarPorPeriodoESituacao(inicio, fim, SituacaoExpedicao.PLANEJADA).get(0).codigo());
                    assertEquals("DEMO-EXP-001", repo.listarPorPeriodoESituacao(inicio, fim, SituacaoExpedicao.CONCLUIDA).get(0).codigo());
                    Long id = em.createQuery("select e.id from Expedicao e where e.codigo = 'DEMO-EXP-001'", Long.class).getSingleResult();
                    assertEquals(2, repo.buscarDetalhesComParticipantes(id).orElseThrow().participantes().size());
                    var coletas = repo.listarColetasDaExpedicao(id);
                    assertEquals(2, coletas.size());
                    for (var coleta : coletas) assertEquals(1, repo.listarAmostrasDaColeta(coleta.getId()).size());
                    for (byte[] arquivo : List.of(repo.baixarMapaRota(id), repo.baixarAutorizacaoAssinada(id), repo.baixarRelatorioFinal(id))) {
                        assertEquals("%PDF-", new String(arquivo, 0, 5, java.nio.charset.StandardCharsets.US_ASCII));
                    }
                    var disponiveis = repo.listarEquipamentosDisponiveis(
                            LocalDateTime.of(2026, 6, 10, 9, 0), LocalDateTime.of(2026, 6, 10, 17, 0));
                    assertEquals(List.of("DEMO-EQP-002"), disponiveis.stream().map(e -> e.getCodigoPatrimonial()).toList());
                    em.find(Expedicao.class, id).setTitulo("Titulo alterado pelo usuario");
                    return null;
                });
                Map<String, Long> antes = contagens(tx);
                assertFalse(tx.executar(CargaInicial::povoar));
                assertEquals(antes, contagens(tx));
                tx.executar(em -> {
                    assertEquals("Titulo alterado pelo usuario", em.createQuery(
                            "select e.titulo from Expedicao e where e.codigo = 'DEMO-EXP-001'", String.class).getSingleResult());
                    assertEquals(1, em.createQuery("select c from Caverna c where c.codigoAmbiental = 'OUTRA-CAVERNA'", Caverna.class).getResultList().size());
                    // Simula uma carga parcialmente removida sem apagar registros.
                    em.createQuery("update Expedicao e set e.codigo = 'RENOMEADA' where e.codigo = 'DEMO-EXP-002'").executeUpdate();
                    return null;
                });
                assertThrows(IllegalStateException.class, () -> tx.executar(CargaInicial::povoar));
                assertEquals(antes, contagens(tx));
            } finally {
                factory.close();
            }
        }
    }

    private Map<String, Long> contagens(TransacaoJta tx) {
        return tx.executar(em -> {
            Map<String, Long> contagens = new java.util.LinkedHashMap<>();
            for (String entidade : List.of("Caverna", "SetorPesquisa", "Pessoa", "Pesquisador", "GuiaEspeleologia",
                    "Equipamento", "Expedicao", "PlanoSeguranca", "ParticipacaoExpedicao", "AutorizacaoAmbiental",
                    "RelatorioFinal", "ColetaCientifica", "Amostra", "UtilizacaoEquipamento")) {
                contagens.put(entidade, em.createQuery("select count(e) from " + entidade + " e", Long.class).getSingleResult());
            }
            return contagens;
        });
    }
}
