package br.edu.ifpb.pweb3.javerna.model.dto;

import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoExpedicao;
import java.time.LocalDateTime;

public record ExpedicaoResumo(String codigo, String titulo, String caverna,
                             LocalDateTime inicioPrevisto, LocalDateTime terminoPrevisto,
                             SituacaoExpedicao situacao) {
}
