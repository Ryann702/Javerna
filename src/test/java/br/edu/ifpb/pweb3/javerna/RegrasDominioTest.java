package br.edu.ifpb.pweb3.javerna;

import br.edu.ifpb.pweb3.javerna.model.entity.*;
import br.edu.ifpb.pweb3.javerna.model.enums.PapelParticipante;
import br.edu.ifpb.pweb3.javerna.model.repository.ExpedicaoConsultaRepository;
import org.junit.jupiter.api.Test;

import static br.edu.ifpb.pweb3.javerna.DadosTeste.*;
import static org.junit.jupiter.api.Assertions.*;

class RegrasDominioTest {

    @Test
    void impedeColetaEmOutraCaverna() {
        Expedicao expedicao = new Expedicao();
        expedicao.setCaverna(caverna("CAV-A"));
        SetorPesquisa setor = new SetorPesquisa();
        setor.setCaverna(caverna("CAV-B"));
        ColetaCientifica coleta = new ColetaCientifica();
        coleta.setSetor(setor);
        coleta.setPesquisadorResponsavel(new Pesquisador());
        assertThrows(IllegalArgumentException.class, () -> expedicao.adicionarColeta(coleta));
        assertTrue(expedicao.getColetas().isEmpty());
    }

    @Test
    void exigeResponsavelPelaRetirada() {
        UtilizacaoEquipamento utilizacao = new UtilizacaoEquipamento();
        utilizacao.setEquipamento(new Equipamento());
        assertThrows(NullPointerException.class, () -> new Expedicao().adicionarUtilizacaoEquipamento(utilizacao));
    }

    @Test
    void impedeParticipacaoDuplicadaPelaIdentidadeDaPessoa() {
        Expedicao expedicao = new Expedicao();
        Pesquisador primeiro = new Pesquisador();
        primeiro.setId(1L);
        Pesquisador mesmaPessoa = new Pesquisador();
        mesmaPessoa.setId(1L);
        expedicao.adicionarParticipacao(participacao(primeiro, PapelParticipante.PESQUISADOR));
        assertThrows(IllegalArgumentException.class, () -> expedicao.adicionarParticipacao(
                participacao(mesmaPessoa, PapelParticipante.COORDENADOR)));
    }

    @Test
    void respeitaQuantidadeMaximaDeParticipantes() {
        Expedicao expedicao = new Expedicao();
        expedicao.setQuantidadeMaximaParticipantes(1);
        expedicao.adicionarParticipacao(participacao(new Pesquisador(), PapelParticipante.PESQUISADOR));
        assertThrows(IllegalArgumentException.class, () -> expedicao.adicionarParticipacao(
                participacao(new GuiaEspeleologia(), PapelParticipante.GUIA)));
    }

    @Test
    void rejeitaPeriodoInvertidoAntesDaConsulta() {
        var repository = new ExpedicaoConsultaRepository(null);
        assertThrows(IllegalArgumentException.class, () -> repository.listarEquipamentosDisponiveis(FIM, INICIO));
    }
}
