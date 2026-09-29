package br.edu.ifpb.pweb3.javerna.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "utilizacao_equipamento")
@Getter
@Setter
@NoArgsConstructor
public class UtilizacaoEquipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora_retirada", nullable = false)
    private LocalDateTime dataHoraRetirada;

    @Column(name = "previsao_devolucao", nullable = false)
    private LocalDateTime previsaoDevolucao;

    @Column(name = "data_hora_devolucao")
    private LocalDateTime dataHoraDevolucao;

    @Column(name = "estado_saida", nullable = false, length = 500)
    private String estadoSaida;

    @Column(name = "estado_retorno", length = 500)
    private String estadoRetorno;

    @Column(name = "custo_avaria", precision = 12, scale = 2)
    private BigDecimal custoAvaria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private Expedicao expedicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Equipamento equipamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_responsavel_id", nullable = false)
    private Pessoa responsavel;

    @PrePersist
    @PreUpdate
    private void validar() {
        if (dataHoraRetirada != null && ((previsaoDevolucao != null && previsaoDevolucao.isBefore(dataHoraRetirada))
                || (dataHoraDevolucao != null && dataHoraDevolucao.isBefore(dataHoraRetirada)))) {
            throw new IllegalStateException("A devolucao nao pode ser anterior a retirada");
        }
        if (custoAvaria != null && custoAvaria.signum() < 0) {
            throw new IllegalStateException("O custo da avaria nao pode ser negativo");
        }
    }
}
