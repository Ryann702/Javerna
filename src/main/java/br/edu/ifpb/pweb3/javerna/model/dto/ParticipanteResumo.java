package br.edu.ifpb.pweb3.javerna.model.dto;

import br.edu.ifpb.pweb3.javerna.model.enums.PapelParticipante;

public record ParticipanteResumo(Long pessoaId, String nome, PapelParticipante papel) {
}
