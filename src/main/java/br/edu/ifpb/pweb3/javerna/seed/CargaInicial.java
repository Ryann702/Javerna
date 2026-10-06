package br.edu.ifpb.pweb3.javerna.seed;

import br.edu.ifpb.pweb3.javerna.model.embeddable.Endereco;
import br.edu.ifpb.pweb3.javerna.model.embeddable.Localizacao;
import br.edu.ifpb.pweb3.javerna.model.entity.*;
import br.edu.ifpb.pweb3.javerna.model.enums.*;
import jakarta.persistence.EntityManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Dados ficticios para demonstracao. Executar dentro de uma transacao JTA. */
public final class CargaInicial {
    private CargaInicial() { }

    public static boolean povoar(EntityManager em) {
        List<String> codigos = List.of(
                "DEMO-EXP-001", "DEMO-EXP-002", "DEMO-EXP-003",
                "DEMO-EXP-004", "DEMO-EXP-005");
        long existentes = em.createQuery("select count(e) from Expedicao e where e.codigo in :codigos", Long.class)
                .setParameter("codigos", codigos).getSingleResult();
        if (existentes == codigos.size()) return false;
        if (existentes == 2) {
            criarCargaComplementar(em, documento());
            em.flush();
            return true;
        }
        if (existentes != 0) {
            throw new IllegalStateException("Carga demonstrativa incompleta. Confira as expedicoes DEMO antes de executar novamente.");
        }
        byte[] documento = documento();
        Caverna caverna = new Caverna();
        caverna.setCodigoAmbiental("DEMO-CAV-001");
        caverna.setNomeOficial("Caverna Horizonte (ficticia)");
        caverna.setMunicipio("Cabaceiras");
        caverna.setUnidadeFederativa("PB");
        caverna.setAltitude(new BigDecimal("400.00"));
        caverna.setExtensaoConhecida(new BigDecimal("850.00"));
        caverna.setDataUltimaInspecao(LocalDate.of(2026, 5, 1));
        caverna.setAcessoPermitido(true);
        caverna.setLocalizacao(new Localizacao(new BigDecimal("-7.480000"), new BigDecimal("-36.280000"), "SIRGAS2000"));
        SetorPesquisa entrada = setor("Galeria de entrada", NivelDificuldade.BAIXO, "10.00");
        SetorPesquisa profundo = setor("Sala profunda", NivelDificuldade.ALTO, "50.00");
        caverna.adicionarSetor(entrada);
        caverna.adicionarSetor(profundo);
        em.persist(caverna);

        Pesquisador pesquisador = new Pesquisador();
        pessoa(pesquisador, "Ana Pesquisa (ficticia)", "00000000001");
        pesquisador.setRegistroInstitucional("DEMO-PES-001");
        pesquisador.setAreaPrincipalPesquisa("Geologia");
        pesquisador.setTitulacao("Doutorado");
        pesquisador.setValorDiarioBolsa(new BigDecimal("150.00"));
        em.persist(pesquisador);
        GuiaEspeleologia guia = new GuiaEspeleologia();
        pessoa(guia, "Bruno Guia (ficticio)", "00000000002");
        guia.setNumeroCredenciamento("DEMO-GUI-001");
        guia.setNivelCertificacao("Avancado");
        guia.setValidadeCertificacao(LocalDate.of(2028, 12, 31));
        guia.setExpedicoesConcluidas(1);
        em.persist(guia);

        Equipamento lanterna = equipamento("DEMO-EQP-001", "Lanterna", TipoEquipamento.ILUMINACAO, SituacaoOperacional.DISPONIVEL);
        Equipamento medidor = equipamento("DEMO-EQP-002", "Medidor de umidade", TipoEquipamento.MEDICAO, SituacaoOperacional.DISPONIVEL);
        Equipamento radio = equipamento("DEMO-EQP-003", "Radio", TipoEquipamento.COMUNICACAO, SituacaoOperacional.EM_MANUTENCAO);
        em.persist(lanterna);
        em.persist(medidor);
        em.persist(radio);

        Expedicao concluida = expedicao(caverna, entrada, "DEMO-EXP-001", LocalDate.of(2026, 6, 10), documento);
        concluida.setSituacao(SituacaoExpedicao.CONCLUIDA);
        concluida.setCustoRealizado(new BigDecimal("850.00"));
        concluida.adicionarSetor(profundo);
        participantes(concluida, pesquisador, guia);
        AutorizacaoAmbiental autorizacao = new AutorizacaoAmbiental();
        autorizacao.setNumero("DEMO-AUT-001");
        autorizacao.setOrgaoEmissor("Orgao ficticio - demonstracao");
        autorizacao.setDataEmissao(LocalDate.of(2026, 5, 20));
        autorizacao.setDataValidade(LocalDate.of(2026, 6, 30));
        autorizacao.setSituacao(SituacaoAutorizacao.EXPIRADA);
        autorizacao.setArquivoAssinado(documento);
        autorizacao.setObservacoes("PDF demonstrativo, sem validade ou assinatura real.");
        concluida.definirAutorizacaoAmbiental(autorizacao);
        RelatorioFinal relatorio = new RelatorioFinal();
        relatorio.setTitulo("Relatorio demonstrativo da expedicao");
        relatorio.setResumo("Duas coletas ficticias para demonstracao das consultas.");
        relatorio.setDataSubmissao(LocalDate.of(2026, 6, 12));
        relatorio.setNumeroTotalPaginas(1);
        relatorio.setSituacao(SituacaoRelatorio.APROVADO);
        relatorio.setArquivoCompleto(documento);
        concluida.definirRelatorioFinal(relatorio);
        coleta(concluida, entrada, pesquisador, "DEMO-AMO-001", true);
        coleta(concluida, profundo, pesquisador, "DEMO-AMO-002", false);
        UtilizacaoEquipamento uso = new UtilizacaoEquipamento();
        uso.setEquipamento(lanterna);
        uso.setResponsavel(guia);
        uso.setDataHoraRetirada(concluida.getInicioPrevisto());
        uso.setPrevisaoDevolucao(concluida.getTerminoPrevisto());
        uso.setDataHoraDevolucao(concluida.getTerminoPrevisto());
        uso.setEstadoSaida("Bom");
        uso.setEstadoRetorno("Bom");
        uso.setCustoAvaria(BigDecimal.ZERO);
        concluida.adicionarUtilizacaoEquipamento(uso);
        em.persist(concluida);

        Expedicao planejada = expedicao(caverna, entrada, "DEMO-EXP-002", LocalDate.of(2026, 10, 10), documento);
        participantes(planejada, pesquisador, guia);
        em.persist(planejada);

        criarCargaComplementar(em, documento);
        em.flush();
        return true;
    }

    private static void criarCargaComplementar(EntityManager em, byte[] documento) {
        Caverna cristal = new Caverna();
        cristal.setCodigoAmbiental("DEMO-CAV-002");
        cristal.setNomeOficial("Caverna Cristalina (ficticia)");
        cristal.setMunicipio("Areia");
        cristal.setUnidadeFederativa("PB");
        cristal.setAltitude(new BigDecimal("520.00"));
        cristal.setExtensaoConhecida(new BigDecimal("1250.00"));
        cristal.setDataUltimaInspecao(LocalDate.of(2026, 6, 18));
        cristal.setAcessoPermitido(true);
        cristal.setLocalizacao(new Localizacao(
                new BigDecimal("-6.960000"), new BigDecimal("-35.700000"), "SIRGAS2000"));
        SetorPesquisa salaoCristais = setor("Salao dos cristais", NivelDificuldade.MODERADO, "25.00");
        SetorPesquisa lagoSubterraneo = setor("Lago subterraneo", NivelDificuldade.ALTO, "42.00");
        SetorPesquisa galeriaSecundaria = setor("Galeria secundaria sem coleta", NivelDificuldade.BAIXO, "12.00");
        cristal.adicionarSetor(salaoCristais);
        cristal.adicionarSetor(lagoSubterraneo);
        cristal.adicionarSetor(galeriaSecundaria);
        em.persist(cristal);

        Caverna semExpedicao = new Caverna();
        semExpedicao.setCodigoAmbiental("DEMO-CAV-003");
        semExpedicao.setNomeOficial("Caverna do Silencio (ficticia)");
        semExpedicao.setMunicipio("Sousa");
        semExpedicao.setUnidadeFederativa("PB");
        semExpedicao.setAltitude(new BigDecimal("310.00"));
        semExpedicao.setExtensaoConhecida(new BigDecimal("430.00"));
        semExpedicao.setDataUltimaInspecao(LocalDate.of(2026, 4, 2));
        semExpedicao.setAcessoPermitido(false);
        semExpedicao.setLocalizacao(new Localizacao(
                new BigDecimal("-6.760000"), new BigDecimal("-38.230000"), "SIRGAS2000"));
        semExpedicao.adicionarSetor(setor("Fissura interditada", NivelDificuldade.EXTREMO, "65.00"));
        em.persist(semExpedicao);

        Pesquisador biologo = new Pesquisador();
        pessoa(biologo, "Carlos Biologia (ficticio)", "00000000003");
        biologo.setRegistroInstitucional("DEMO-PES-002");
        biologo.setAreaPrincipalPesquisa("Biologia");
        biologo.setTitulacao("Mestrado");
        biologo.setValorDiarioBolsa(new BigDecimal("130.00"));
        em.persist(biologo);

        Pesquisador semColeta = new Pesquisador();
        pessoa(semColeta, "Daniela Cartografia (ficticia)", "00000000004");
        semColeta.setRegistroInstitucional("DEMO-PES-003");
        semColeta.setAreaPrincipalPesquisa("Cartografia");
        semColeta.setTitulacao("Graduacao");
        semColeta.setValorDiarioBolsa(new BigDecimal("110.00"));
        em.persist(semColeta);

        GuiaEspeleologia guia = new GuiaEspeleologia();
        pessoa(guia, "Elisa Guia (ficticia)", "00000000005");
        guia.setNumeroCredenciamento("DEMO-GUI-002");
        guia.setNivelCertificacao("Intermediario");
        guia.setValidadeCertificacao(LocalDate.of(2029, 6, 30));
        guia.setExpedicoesConcluidas(8);
        em.persist(guia);

        Equipamento camera = equipamento(
                "DEMO-EQP-004", "Camera termica", TipoEquipamento.MEDICAO, SituacaoOperacional.EM_USO);
        Equipamento kitColeta = equipamento(
                "DEMO-EQP-005", "Kit de coleta", TipoEquipamento.COLETA, SituacaoOperacional.INDISPONIVEL);
        Equipamento corda = equipamento(
                "DEMO-EQP-006", "Corda de seguranca", TipoEquipamento.SEGURANCA, SituacaoOperacional.EM_MANUTENCAO);
        em.persist(camera);
        em.persist(kitColeta);
        em.persist(corda);

        Expedicao autorizada = expedicao(
                cristal, salaoCristais, "DEMO-EXP-003", LocalDate.of(2026, 7, 15), documento);
        autorizada.setSituacao(SituacaoExpedicao.AUTORIZADA);
        autorizada.adicionarSetor(lagoSubterraneo);
        participantes(autorizada, biologo, guia);
        coleta(autorizada, salaoCristais, biologo, "DEMO-AMO-003", true);
        coleta(autorizada, lagoSubterraneo, biologo, "DEMO-AMO-004", false);
        autorizada.definirAutorizacaoAmbiental(autorizacao(
                "DEMO-AUT-002", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1), documento));

        UtilizacaoEquipamento usoCamera = new UtilizacaoEquipamento();
        usoCamera.setEquipamento(camera);
        usoCamera.setResponsavel(guia);
        usoCamera.setDataHoraRetirada(autorizada.getInicioPrevisto());
        usoCamera.setPrevisaoDevolucao(autorizada.getTerminoPrevisto());
        usoCamera.setEstadoSaida("Bom");
        usoCamera.setCustoAvaria(BigDecimal.ZERO);
        autorizada.adicionarUtilizacaoEquipamento(usoCamera);
        em.persist(autorizada);

        Expedicao cancelada = expedicao(
                cristal, galeriaSecundaria, "DEMO-EXP-004", LocalDate.of(2026, 8, 20), documento);
        cancelada.setSituacao(SituacaoExpedicao.CANCELADA);
        cancelada.setCancelamentoEmergencial(true);
        em.persist(cancelada);

        Expedicao emAndamento = expedicao(
                cristal, salaoCristais, "DEMO-EXP-005", LocalDate.of(2026, 9, 5), documento);
        emAndamento.setSituacao(SituacaoExpedicao.EM_ANDAMENTO);
        emAndamento.adicionarSetor(lagoSubterraneo);
        participantes(emAndamento, biologo, guia);
        coleta(emAndamento, salaoCristais, biologo, "DEMO-AMO-005", true);
        coleta(emAndamento, lagoSubterraneo, biologo, "DEMO-AMO-006", true);
        em.persist(emAndamento);
    }

    private static AutorizacaoAmbiental autorizacao(
            String numero, LocalDate emissao, LocalDate validade, byte[] documento) {
        AutorizacaoAmbiental autorizacao = new AutorizacaoAmbiental();
        autorizacao.setNumero(numero);
        autorizacao.setOrgaoEmissor("Orgao ficticio - demonstracao");
        autorizacao.setDataEmissao(emissao);
        autorizacao.setDataValidade(validade);
        autorizacao.setSituacao(SituacaoAutorizacao.EMITIDA);
        autorizacao.setArquivoAssinado(documento);
        autorizacao.setObservacoes("Documento ficticio para demonstracao.");
        return autorizacao;
    }

    private static SetorPesquisa setor(String nome, NivelDificuldade nivel, String profundidade) {
        SetorPesquisa setor = new SetorPesquisa();
        setor.setDenominacao(nome);
        setor.setNivelDificuldade(nivel);
        setor.setProfundidadeMaxima(new BigDecimal(profundidade));
        setor.setExtensaoAproximada(new BigDecimal("300.00"));
        setor.setDescricao("Setor ficticio para demonstracao");
        setor.setCondicaoAtual("Liberado");
        return setor;
    }

    private static void pessoa(Pessoa pessoa, String nome, String cpf) {
        pessoa.setNome(nome);
        pessoa.setCpf(cpf);
        pessoa.setDataNascimento(LocalDate.of(1990, 1, 1));
        pessoa.setEmail("demo" + cpf + "@example.org");
        pessoa.setTelefone("00000000000");
        pessoa.setAtiva(true);
        pessoa.setEndereco(new Endereco("Rua Ficticia", "10", null, "Centro", "Cabaceiras", "PB", "00000000"));
    }

    private static Equipamento equipamento(String codigo, String nome, TipoEquipamento tipo, SituacaoOperacional situacao) {
        Equipamento equipamento = new Equipamento();
        equipamento.setCodigoPatrimonial(codigo);
        equipamento.setNome(nome);
        equipamento.setTipo(tipo);
        equipamento.setFabricante("Fabricante ficticio");
        equipamento.setValorAquisicao(new BigDecimal("500.00"));
        equipamento.setDataCompra(LocalDate.of(2026, 1, 10));
        equipamento.setSituacaoOperacional(situacao);
        return equipamento;
    }

    private static Expedicao expedicao(Caverna caverna, SetorPesquisa setor, String codigo, LocalDate data, byte[] documento) {
        Expedicao expedicao = new Expedicao();
        expedicao.setCodigo(codigo);
        expedicao.setTitulo("Expedicao demonstrativa " + codigo);
        expedicao.setObjetivo("Demonstrar o modelo de persistencia com dados ficticios");
        expedicao.setInicioPrevisto(data.atTime(8, 0));
        expedicao.setTerminoPrevisto(data.atTime(18, 0));
        expedicao.setOrcamentoAprovado(new BigDecimal("1000.00"));
        expedicao.setQuantidadeMaximaParticipantes(5);
        caverna.adicionarExpedicao(expedicao);
        expedicao.adicionarSetor(setor);
        PlanoSeguranca plano = new PlanoSeguranca();
        plano.setProcedimentosEvacuacao("Exemplo ficticio: retornar pela entrada");
        plano.setPontoExternoEncontro("Base ficticia");
        plano.setTempoMaximoSemComunicacao(30);
        plano.setTelefoneEmergencia("00000000000");
        plano.setMapaRota(documento);
        expedicao.definirPlanoSeguranca(plano);
        return expedicao;
    }

    private static void participantes(Expedicao expedicao, Pesquisador pesquisador, GuiaEspeleologia guia) {
        for (Pessoa pessoa : List.of(pesquisador, guia)) {
            ParticipacaoExpedicao participacao = new ParticipacaoExpedicao();
            participacao.setPessoa(pessoa);
            participacao.setPapel(pessoa instanceof Pesquisador ? PapelParticipante.PESQUISADOR : PapelParticipante.GUIA);
            participacao.setDataConfirmacao(expedicao.getInicioPrevisto().toLocalDate().minusDays(1));
            participacao.setValorDiaria(new BigDecimal("100.00"));
            participacao.setQuantidadePrevistaDias(1);
            participacao.setPresencaConfirmada(true);
            expedicao.adicionarParticipacao(participacao);
        }
    }

    private static void coleta(Expedicao expedicao, SetorPesquisa setor, Pesquisador pesquisador, String codigo, boolean medida) {
        ColetaCientifica coleta = new ColetaCientifica();
        coleta.setSetor(setor);
        coleta.setPesquisadorResponsavel(pesquisador);
        coleta.setDataHora(expedicao.getInicioPrevisto().plusHours(2));
        coleta.setMetodoEmpregado("Coleta manual");
        coleta.setDescricaoPonto("Ponto ficticio de " + setor.getDenominacao());
        if (medida) {
            coleta.setTemperatura(new BigDecimal("23.50"));
            coleta.setUmidadeRelativa(new BigDecimal("80.00"));
            coleta.setProfundidade(new BigDecimal("8.25"));
        }
        Amostra amostra = new Amostra();
        amostra.setCodigoCampo(codigo);
        amostra.setCategoria(CategoriaAmostra.ROCHA);
        amostra.setMassaOuVolume(new BigDecimal("12.3456"));
        amostra.setUnidadeMedida("g");
        amostra.setDataAcondicionamento(coleta.getDataHora().toLocalDate());
        amostra.setCondicaoConservacao(CondicaoConservacao.AMBIENTE);
        coleta.adicionarAmostra(amostra);
        expedicao.adicionarColeta(coleta);
    }

    private static byte[] documento() {
        try (var arquivo = CargaInicial.class.getResourceAsStream("/seed/demonstracao.pdf")) {
            if (arquivo == null) throw new IllegalStateException("Documento demonstrativo nao encontrado");
            return arquivo.readAllBytes();
        } catch (IOException erro) {
            throw new IllegalStateException("Erro ao ler documento demonstrativo", erro);
        }
    }
}
