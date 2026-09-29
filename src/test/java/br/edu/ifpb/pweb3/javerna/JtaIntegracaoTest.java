package br.edu.ifpb.pweb3.javerna;

import br.edu.ifpb.pweb3.javerna.persistence.TransacaoJta;
import br.edu.ifpb.pweb3.javerna.model.entity.Caverna;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JtaIntegracaoTest {

    @Test
    void confirmaEDesfazTransacoesNoPostgres() throws Exception {
        try (EmbeddedPostgres postgres = EmbeddedPostgres.builder().start()) {
            EntityManagerFactory factory = Persistence.createEntityManagerFactory("javernaPU", Map.of(
                    "jakarta.persistence.jdbc.url", postgres.getJdbcUrl("postgres", "postgres"),
                    "jakarta.persistence.jdbc.password", "",
                    "hibernate.hbm2ddl.auto", "create-drop"));
            try {
                TransacaoJta transacao = new TransacaoJta(factory);
                transacao.executar(em -> {
                    em.createNativeQuery("create table teste_jta (id integer primary key)").executeUpdate();
                    em.createNativeQuery("insert into teste_jta values (1)").executeUpdate();
                    return null;
                });
                assertThrows(IllegalStateException.class, () -> transacao.executar(em -> {
                    em.createNativeQuery("insert into teste_jta values (2)").executeUpdate();
                    throw new IllegalArgumentException("Falha depois do INSERT");
                }));
                long quantidade = transacao.executar(em -> ((Number) em.createNativeQuery(
                        "select count(*) from teste_jta").getSingleResult()).longValue());
                assertEquals(1, quantidade, "Somente o INSERT confirmado deve permanecer");
                assertThrows(IllegalStateException.class, () -> transacao.executar(em -> {
                    Caverna caverna = DadosTeste.caverna("CAV-ROLLBACK");
                    em.persist(caverna);
                    em.flush();
                    throw new IllegalArgumentException("Falha depois de persist e flush");
                }));
                long cavernas = transacao.executar(em -> em.createQuery(
                        "select count(c) from Caverna c where c.codigoAmbiental = 'CAV-ROLLBACK'", Long.class)
                        .getSingleResult());
                assertEquals(0, cavernas);
            } finally {
                factory.close();
            }
        }
    }
}
