package br.edu.ifpb.pweb3.javerna.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

import java.util.function.Function;

public final class TransacaoJta {

    private final EntityManagerFactory factory;

    public TransacaoJta(EntityManagerFactory factory) {
        this.factory = factory;
    }

    public <T> T executar(Function<EntityManager, T> operacao) {
        UserTransaction tx = com.arjuna.ats.jta.UserTransaction.userTransaction();
        EntityManager em = null;
        boolean iniciou = false;
        try {
            if (tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
                throw new IllegalStateException("Ja existe uma transacao nesta thread");
            }
            tx.begin();
            iniciou = true;
            em = factory.createEntityManager();
            em.joinTransaction();
            T resultado = operacao.apply(em);
            tx.commit();
            return resultado;
        } catch (Exception | Error erro) {
            if (iniciou) {
                try {
                    int status = tx.getStatus();
                    if (status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK) {
                        tx.rollback();
                    }
                } catch (Exception rollbackException) {
                    erro.addSuppressed(rollbackException);
                }
            }
            if (erro instanceof Error fatal) {
                throw fatal;
            }
            throw new IllegalStateException("Erro ao executar transacao JTA", erro);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
