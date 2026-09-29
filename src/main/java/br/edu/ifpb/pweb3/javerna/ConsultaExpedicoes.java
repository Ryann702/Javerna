package br.edu.ifpb.pweb3.javerna;

import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoExpedicao;
import br.edu.ifpb.pweb3.javerna.model.repository.ExpedicaoConsultaRepository;
import br.edu.ifpb.pweb3.javerna.persistence.TransacaoJta;
import br.edu.ifpb.pweb3.javerna.seed.CargaInicial;
import jakarta.persistence.Persistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ConsultaExpedicoes {

    public static void main(String[] args) {
        boolean povoar = args.length == 1 && "--povoar".equals(args[0]);
        if (!povoar && args.length != 3) {
            throw new IllegalArgumentException("Use --povoar ou informe inicio, fim (AAAA-MM-DDTHH:mm) e situacao");
        }
        LocalDateTime inicio = povoar ? null : LocalDateTime.parse(args[0]);
        LocalDateTime fim = povoar ? null : LocalDateTime.parse(args[1]);
        SituacaoExpedicao situacao = povoar ? null : SituacaoExpedicao.valueOf(args[2]);
        System.setProperty("ObjectStoreEnvironmentBean.objectStoreDir",
                System.getProperty("ObjectStoreEnvironmentBean.objectStoreDir", ".javerna/narayana"));
        Map<String, Object> propriedades = new HashMap<>();
        configurar(propriedades, "JAVERNA_DB_URL", "jakarta.persistence.jdbc.url");
        configurar(propriedades, "JAVERNA_DB_USER", "jakarta.persistence.jdbc.user");
        configurar(propriedades, "JAVERNA_DB_PASSWORD", "jakarta.persistence.jdbc.password");
        var factory = Persistence.createEntityManagerFactory("javernaPU", propriedades);
        try {
            if (povoar) {
                boolean criada = new TransacaoJta(factory).executar(CargaInicial::povoar);
                System.out.println(criada ? "Carga inicial criada: DEMO-EXP-001 e DEMO-EXP-002."
                        : "Carga inicial ja existente. Nenhum dado foi alterado.");
                return;
            }
            var expedicoes = new TransacaoJta(factory).executar(em ->
                    new ExpedicaoConsultaRepository(em).listarPorPeriodoESituacao(inicio, fim, situacao));
            expedicoes.forEach(System.out::println);
            System.out.println("Expedicoes encontradas: " + expedicoes.size());
        } finally {
            factory.close();
        }
    }

    private static void configurar(Map<String, Object> propriedades, String variavel, String propriedade) {
        String valor = System.getenv(variavel);
        if (valor != null) propriedades.put(propriedade, valor);
    }
}
