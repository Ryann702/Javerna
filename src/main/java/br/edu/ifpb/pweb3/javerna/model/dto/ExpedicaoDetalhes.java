package br.edu.ifpb.pweb3.javerna.model.dto;

import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoExpedicao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExpedicaoDetalhes(Long id, String codigo, String titulo, String objetivo,
                               String caverna, LocalDateTime inicioPrevisto,
                               LocalDateTime terminoPrevisto, BigDecimal orcamentoAprovado,
                               BigDecimal custoRealizado, Integer quantidadeMaximaParticipantes,
                               SituacaoExpedicao situacao, boolean cancelamentoEmergencial,
                               List<ParticipanteResumo> participantes) {

    public ExpedicaoDetalhes {
        participantes = List.copyOf(participantes);
    }

    public ExpedicaoDetalhes(Long id, String codigo, String titulo, String objetivo,
                             String caverna, LocalDateTime inicioPrevisto,
                             LocalDateTime terminoPrevisto, BigDecimal orcamentoAprovado,
                             BigDecimal custoRealizado, Integer quantidadeMaximaParticipantes,
                             SituacaoExpedicao situacao, boolean cancelamentoEmergencial) {
        this(id, codigo, titulo, objetivo, caverna, inicioPrevisto, terminoPrevisto,
                orcamentoAprovado, custoRealizado, quantidadeMaximaParticipantes,
                situacao, cancelamentoEmergencial, List.of());
    }

    public ExpedicaoDetalhes comParticipantes(List<ParticipanteResumo> equipe) {
        return new ExpedicaoDetalhes(id, codigo, titulo, objetivo, caverna, inicioPrevisto,
                terminoPrevisto, orcamentoAprovado, custoRealizado, quantidadeMaximaParticipantes,
                situacao, cancelamentoEmergencial, equipe);
    }
}
