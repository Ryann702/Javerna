package br.edu.ifpb.pweb3.javerna.model.entity;

import br.edu.ifpb.pweb3.javerna.model.enums.PapelParticipante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "participacao_expedicao",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_participacao_expedicao_pessoa",
                columnNames = {"expedicao_id", "pessoa_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ParticipacaoExpedicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PapelParticipante papel;

    @Column(name = "data_confirmacao", nullable = false)
    private LocalDate dataConfirmacao;

    @Column(name = "valor_diaria", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorDiaria;

    @Column(name = "quantidade_prevista_dias", nullable = false)
    private Integer quantidadePrevistaDias;

    @Column(name = "presenca_confirmada", nullable = false)
    private boolean presencaConfirmada;

    @Column(length = 1000)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private Expedicao expedicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;
}
