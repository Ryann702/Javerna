package br.edu.ifpb.pweb3.javerna.repository;

import br.edu.ifpb.pweb3.javerna.model.entity.Amostra;
import br.edu.ifpb.pweb3.javerna.model.entity.ColetaCientifica;
import br.edu.ifpb.pweb3.javerna.model.entity.Equipamento;
import br.edu.ifpb.pweb3.javerna.model.entity.Expedicao;
import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoExpedicao;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ExpedicaoConsultaRepository {

    private final EntityManager entityManager;

    public ExpedicaoConsultaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Expedicao> listarPorPeriodoESituacao(LocalDateTime dataInicio,
                                                       LocalDateTime dataFim,
                                                       SituacaoExpedicao situacao) {
        return entityManager
                .createNamedQuery("Expedicao.listarPorPeriodoESituacao", Expedicao.class)
                .setParameter("dataInicio", dataInicio)
                .setParameter("dataFim", dataFim)
                .setParameter("situacao", situacao)
                .getResultList();
    }

    public Optional<Expedicao> buscarDetalhesComParticipantes(Long expedicaoId) {
        return entityManager
                .createNamedQuery("Expedicao.buscarDetalhesComParticipantes", Expedicao.class)
                .setParameter("id", expedicaoId)
                .getResultStream()
                .findFirst();
    }

    public List<ColetaCientifica> listarColetasDaExpedicao(Long expedicaoId) {
        return entityManager
                .createNamedQuery("ColetaCientifica.listarPorExpedicao", ColetaCientifica.class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultList();
    }

    public List<Equipamento> listarEquipamentosDisponiveis(LocalDateTime inicio, LocalDateTime fim) {
        return entityManager
                .createNamedQuery("Equipamento.listarDisponiveisNoPeriodo", Equipamento.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList();
    }

    public List<Amostra> listarAmostrasDaColeta(Long coletaId) {
        return entityManager
                .createQuery("SELECT a FROM Amostra a WHERE a.coleta.id = :coletaId", Amostra.class)
                .setParameter("coletaId", coletaId)
                .getResultList();
    }

    public byte[] baixarMapaRota(Long expedicaoId) {
        Expedicao expedicao = entityManager.find(Expedicao.class, expedicaoId);
        return expedicao == null ? null : expedicao.getPlanoSeguranca().getMapaRota();
    }

    public byte[] baixarAutorizacaoAssinada(Long expedicaoId) {
        Expedicao expedicao = entityManager.find(Expedicao.class, expedicaoId);
        if (expedicao == null || expedicao.getAutorizacaoAmbiental() == null) {
            return null;
        }
        return expedicao.getAutorizacaoAmbiental().getArquivoAssinado();
    }

    public byte[] baixarRelatorioFinal(Long expedicaoId) {
        Expedicao expedicao = entityManager.find(Expedicao.class, expedicaoId);
        if (expedicao == null || expedicao.getRelatorioFinal() == null) {
            return null;
        }
        return expedicao.getRelatorioFinal().getArquivoCompleto();
    }
}