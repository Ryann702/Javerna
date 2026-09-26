package br.edu.ifpb.pweb3.javerna.model.entity;

import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoOperacional;
import br.edu.ifpb.pweb3.javerna.model.enums.TipoEquipamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipamento")
@Getter
@Setter
@NoArgsConstructor
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_patrimonial", nullable = false, unique = true, length = 40)
    private String codigoPatrimonial;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEquipamento tipo;

    @Column(nullable = false, length = 100)
    private String fabricante;

    @Column(name = "valor_aquisicao", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAquisicao;

    @Column(name = "data_compra", nullable = false)
    private LocalDate dataCompra;

    @Column(name = "data_ultima_manutencao")
    private LocalDate dataUltimaManutencao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_operacional", nullable = false, length = 20)
    private SituacaoOperacional situacaoOperacional;

    @Column(name = "exige_calibracao", nullable = false)
    private boolean exigeCalibracao;


    @OneToMany(mappedBy = "equipamento", fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private List<UtilizacaoEquipamento> utilizacoes = new ArrayList<>();

    void vincularUtilizacao(UtilizacaoEquipamento utilizacao) {
        utilizacoes.add(utilizacao);
    }

    void desvincularUtilizacao(UtilizacaoEquipamento utilizacao) {
        utilizacoes.remove(utilizacao);
    }
}