package br.edu.ifpb.pweb3.javerna.model.repository;

import br.edu.ifpb.pweb3.javerna.model.dto.ExpedicaoResumo;
import br.edu.ifpb.pweb3.javerna.model.dto.ExpedicaoDetalhes;
import br.edu.ifpb.pweb3.javerna.model.dto.ParticipanteResumo;
import br.edu.ifpb.pweb3.javerna.model.entity.Amostra;
import br.edu.ifpb.pweb3.javerna.model.entity.ColetaCientifica;
import br.edu.ifpb.pweb3.javerna.model.entity.Equipamento;
import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoExpedicao;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class ExpedicaoConsultaRepository {

    private final EntityManager entityManager;

    public ExpedicaoConsultaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<ExpedicaoResumo> listarPorPeriodoESituacao(LocalDateTime dataInicio,
                                                        LocalDateTime dataFim,
                                                        SituacaoExpedicao situacao) {
        validarPeriodo(dataInicio, dataFim);
        Objects.requireNonNull(situacao, "A situacao e obrigatoria");
        return entityManager
                .createNamedQuery("Expedicao.listarPorPeriodoESituacao", ExpedicaoResumo.class)
                .setParameter("dataInicio", dataInicio)
                .setParameter("dataFim", dataFim)
                .setParameter("situacao", situacao)
                .getResultList();
    }

    public Optional<ExpedicaoDetalhes> buscarDetalhesComParticipantes(Long expedicaoId) {
        List<ExpedicaoDetalhes> detalhes = entityManager
                .createNamedQuery("Expedicao.buscarDetalhes", ExpedicaoDetalhes.class)
                .setParameter("id", expedicaoId)
                .getResultList();
        if (detalhes.isEmpty()) {
            return Optional.empty();
        }
        List<ParticipanteResumo> participantes = entityManager
                .createNamedQuery("ParticipacaoExpedicao.listarResumos", ParticipanteResumo.class)
                .setParameter("id", expedicaoId)
                .getResultList();
        return Optional.of(detalhes.get(0).comParticipantes(participantes));
    }

    public List<ColetaCientifica> listarColetasDaExpedicao(Long expedicaoId) {
        return entityManager
                .createNamedQuery("ColetaCientifica.listarPorExpedicao", ColetaCientifica.class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultList();
    }

    public List<Equipamento> listarEquipamentosDisponiveis(LocalDateTime inicio, LocalDateTime fim) {
        validarPeriodo(inicio, fim);
        return entityManager
                .createNamedQuery("Equipamento.listarDisponiveisNoPeriodo", Equipamento.class)
                .setParameter("inicio", inicio)
                .setParameter("fim", fim)
                .getResultList();
    }

    public List<Amostra> listarAmostrasDaColeta(Long coletaId) {
        return entityManager
                .createNamedQuery("Amostra.listarPorColeta", Amostra.class)
                .setParameter("coletaId", coletaId)
                .getResultList();
    }

    public byte[] baixarMapaRota(Long expedicaoId) {
        return baixarArquivo("PlanoSeguranca.baixarMapa", expedicaoId);
    }

    public byte[] baixarAutorizacaoAssinada(Long expedicaoId) {
        return baixarArquivo("AutorizacaoAmbiental.baixarArquivo", expedicaoId);
    }

    public byte[] baixarRelatorioFinal(Long expedicaoId) {
        return baixarArquivo("RelatorioFinal.baixarArquivo", expedicaoId);
    }

    private byte[] baixarArquivo(String consulta, Long expedicaoId) {
        List<byte[]> arquivos = entityManager.createNamedQuery(consulta, byte[].class)
                .setParameter("expedicaoId", expedicaoId)
                .getResultList();
        return arquivos.isEmpty() ? null : arquivos.get(0);
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Objects.requireNonNull(inicio, "O inicio e obrigatorio");
        Objects.requireNonNull(fim, "O fim e obrigatorio");
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("O fim nao pode ser anterior ao inicio");
        }
    }
}
