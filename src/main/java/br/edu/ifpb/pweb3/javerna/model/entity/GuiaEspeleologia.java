package br.edu.ifpb.pweb3.javerna.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "guia_espeleologia")
@PrimaryKeyJoinColumn(name = "pessoa_id")
@Getter
@Setter
@NoArgsConstructor
public class GuiaEspeleologia extends Pessoa {

    @Column(name = "numero_credenciamento", nullable = false, unique = true, length = 30)
    private String numeroCredenciamento;

    @Column(name = "nivel_certificacao", nullable = false, length = 50)
    private String nivelCertificacao;

    @Column(name = "validade_certificacao", nullable = false)
    private LocalDate validadeCertificacao;

    @Column(name = "expedicoes_concluidas", nullable = false)
    private Integer expedicoesConcluidas;
}
