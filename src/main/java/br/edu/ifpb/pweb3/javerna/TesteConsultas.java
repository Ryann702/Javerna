package br.edu.ifpb.pweb3.javerna;

import br.edu.ifpb.pweb3.javerna.model.entity.ColetaCientifica;
import br.edu.ifpb.pweb3.javerna.persistence.TransacaoJta;
import jakarta.persistence.Persistence;

public class TesteConsultas {

    public static void main(String[] args) {
        var factory =
            Persistence.createEntityManagerFactory("javernaPU");

        try {
            var resultado = new TransacaoJta(factory).executar(em ->
                em.createQuery("""
                    SELECT c
                    FROM ColetaCientifica c
                    WHERE c.setor.id = :setorId
                    ORDER BY c.dataHora DESC
                    """, ColetaCientifica.class)
                    .setParameter("setorId", 1L)
                    .getResultList()
            );

            for (ColetaCientifica coleta : resultado) {
                System.out.println("Id: " + coleta.getId());
                System.out.println("Data: " + coleta.getDataHora());
                System.out.println("Metodo: " + coleta.getMetodoEmpregado());
                System.out.println("Ponto: " + coleta.getDescricaoPonto());
                System.out.println("Validacao: " + coleta.getSituacaoValidacao());
                System.out.println();
            }

            System.out.println("Total: " + resultado.size());
        } finally {
            factory.close();
        }
    }
}
