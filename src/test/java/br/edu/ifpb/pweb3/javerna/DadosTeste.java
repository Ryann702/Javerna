package br.edu.ifpb.pweb3.javerna;

import br.edu.ifpb.pweb3.javerna.model.embeddable.Endereco;
import br.edu.ifpb.pweb3.javerna.model.embeddable.Localizacao;
import br.edu.ifpb.pweb3.javerna.model.entity.*;
import br.edu.ifpb.pweb3.javerna.model.enums.*;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

final class DadosTeste {
    static final LocalDateTime INICIO = LocalDateTime.of(2026, 6, 10, 8, 0);
    static final LocalDateTime FIM = INICIO.plusDays(5);
    static final byte[] MAPA = {1, 2, 3};
    static final byte[] AUTORIZACAO = {4, 5, 6};
    static final byte[] RELATORIO = {7, 8, 9};
    static final byte[] FOTO = {10, 11, 12};
    private static final AtomicInteger SEQUENCIA = new AtomicInteger();

    record Ids(Long expedicao, Long coleta, Long amostra, Long pesquisador,
               Long guia, Long caverna, Long setor, Long plano) { }

    static Ids criar(EntityManager em) {
        int numero = SEQUENCIA.incrementAndGet();
        Caverna caverna = caverna("CAV-" + numero);
        SetorPesquisa setor = new SetorPesquisa();
        setor.setDenominacao("Galeria principal");
        setor.setNivelDificuldade(NivelDificuldade.MODERADO);
        setor.setProfundidadeMaxima(new BigDecimal("35.50"));
        setor.setExtensaoAproximada(new BigDecimal("500.00"));
        setor.setDescricao("Galeria de pesquisa");
        setor.setCondicaoAtual("Liberado");
        caverna.adicionarSetor(setor);
        em.persist(caverna);

        Pesquisador pesquisador = new Pesquisador();
        preencherPessoa(pesquisador, numero * 10L);
        pesquisador.setRegistroInstitucional("PES-" + numero);
        pesquisador.setAreaPrincipalPesquisa("Geologia");
        pesquisador.setTitulacao("Doutorado");
        pesquisador.setValorDiarioBolsa(new BigDecimal("150.00"));
        em.persist(pesquisador);
        GuiaEspeleologia guia = new GuiaEspeleologia();
        preencherPessoa(guia, numero * 10L + 1);
        guia.setNumeroCredenciamento("GUIA-" + numero);
        guia.setNivelCertificacao("Avancado");
        guia.setValidadeCertificacao(LocalDate.of(2028, 1, 1));
        guia.setExpedicoesConcluidas(10);
        em.persist(guia);

        Expedicao expedicao = new Expedicao();
        expedicao.setCodigo("EXP-" + numero);
        expedicao.setTitulo("Pesquisa subterranea");
        expedicao.setObjetivo("Caracterizar as rochas");
        expedicao.setInicioPrevisto(INICIO);
        expedicao.setTerminoPrevisto(FIM);
        expedicao.setOrcamentoAprovado(new BigDecimal("1000.00"));
        expedicao.setQuantidadeMaximaParticipantes(10);
        caverna.adicionarExpedicao(expedicao);
        expedicao.adicionarSetor(setor);

        PlanoSeguranca plano = new PlanoSeguranca();
        plano.setProcedimentosEvacuacao("Retornar pela entrada principal");
        plano.setPontoExternoEncontro("Base");
        plano.setTempoMaximoSemComunicacao(60);
        plano.setTelefoneEmergencia("83999999999");
        plano.setMapaRota(MAPA);
        expedicao.definirPlanoSeguranca(plano);

        AutorizacaoAmbiental autorizacao = new AutorizacaoAmbiental();
        autorizacao.setNumero("AUT-" + numero);
        autorizacao.setOrgaoEmissor("Orgao ambiental");
        autorizacao.setDataEmissao(INICIO.toLocalDate().minusDays(10));
        autorizacao.setDataValidade(FIM.toLocalDate().plusDays(10));
        autorizacao.setSituacao(SituacaoAutorizacao.EMITIDA);
        autorizacao.setArquivoAssinado(AUTORIZACAO);
        expedicao.definirAutorizacaoAmbiental(autorizacao);

        RelatorioFinal relatorio = new RelatorioFinal();
        relatorio.setTitulo("Resultados");
        relatorio.setResumo("Caracterizacao das amostras");
        relatorio.setDataSubmissao(FIM.toLocalDate());
        relatorio.setNumeroTotalPaginas(5);
        relatorio.setSituacao(SituacaoRelatorio.SUBMETIDO);
        relatorio.setArquivoCompleto(RELATORIO);
        expedicao.definirRelatorioFinal(relatorio);
        expedicao.adicionarParticipacao(participacao(pesquisador, PapelParticipante.PESQUISADOR));
        expedicao.adicionarParticipacao(participacao(guia, PapelParticipante.GUIA));

        ColetaCientifica primeira = coleta(setor, pesquisador);
        Amostra amostra = new Amostra();
        amostra.setCodigoCampo("AMO-" + numero);
        amostra.setCategoria(CategoriaAmostra.ROCHA);
        amostra.setMassaOuVolume(new BigDecimal("12.3456"));
        amostra.setUnidadeMedida("g");
        amostra.setDataAcondicionamento(INICIO.toLocalDate());
        amostra.setCondicaoConservacao(CondicaoConservacao.AMBIENTE);
        amostra.setFotografia(FOTO);
        primeira.adicionarAmostra(amostra);
        expedicao.adicionarColeta(primeira);
        expedicao.adicionarColeta(coleta(setor, pesquisador));
        em.persist(expedicao);
        em.flush();
        return new Ids(expedicao.getId(), primeira.getId(), amostra.getId(), pesquisador.getId(),
                guia.getId(), caverna.getId(), setor.getId(), plano.getId());
    }

    static Caverna caverna(String codigo) {
        Caverna caverna = new Caverna();
        caverna.setNomeOficial("Caverna de teste");
        caverna.setCodigoAmbiental(codigo);
        caverna.setMunicipio("Cabaceiras");
        caverna.setUnidadeFederativa("PB");
        caverna.setAltitude(new BigDecimal("400.00"));
        caverna.setExtensaoConhecida(new BigDecimal("500.00"));
        caverna.setDataUltimaInspecao(LocalDate.of(2026, 1, 1));
        caverna.setAcessoPermitido(true);
        caverna.setLocalizacao(new Localizacao(new BigDecimal("-7.488900"),
                new BigDecimal("-36.287200"), "SIRGAS2000"));
        return caverna;
    }

    static ParticipacaoExpedicao participacao(Pessoa pessoa, PapelParticipante papel) {
        ParticipacaoExpedicao participacao = new ParticipacaoExpedicao();
        participacao.setPessoa(pessoa);
        participacao.setPapel(papel);
        participacao.setDataConfirmacao(INICIO.toLocalDate().minusDays(1));
        participacao.setValorDiaria(new BigDecimal("100.00"));
        participacao.setQuantidadePrevistaDias(5);
        participacao.setPresencaConfirmada(true);
        return participacao;
    }

    private static ColetaCientifica coleta(SetorPesquisa setor, Pesquisador pesquisador) {
        ColetaCientifica coleta = new ColetaCientifica();
        coleta.setSetor(setor);
        coleta.setPesquisadorResponsavel(pesquisador);
        coleta.setDataHora(INICIO.plusHours(2));
        coleta.setMetodoEmpregado("Coleta manual");
        coleta.setDescricaoPonto("Galeria principal");
        coleta.setTemperatura(new BigDecimal("23.50"));
        coleta.setUmidadeRelativa(new BigDecimal("80.00"));
        coleta.setProfundidade(new BigDecimal("30.25"));
        return coleta;
    }

    private static void preencherPessoa(Pessoa pessoa, long numero) {
        pessoa.setNome("Pessoa " + numero);
        pessoa.setCpf(String.format("%011d", numero));
        pessoa.setDataNascimento(LocalDate.of(1990, 1, 1));
        pessoa.setEmail("pessoa" + numero + "@example.org");
        pessoa.setTelefone("83999999999");
        pessoa.setAtiva(true);
        pessoa.setEndereco(new Endereco("Rua A", "10", null, "Centro", "Campina Grande", "PB", "58000000"));
    }
}
