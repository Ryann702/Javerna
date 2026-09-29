package br.edu.ifpb.pweb3.javerna.model.entity;

import br.edu.ifpb.pweb3.javerna.model.enums.CategoriaAmostra;
import br.edu.ifpb.pweb3.javerna.model.enums.CondicaoConservacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "amostra")
@Getter
@Setter
@NoArgsConstructor
public class Amostra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_campo", nullable = false, unique = true, length = 40)
    private String codigoCampo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaAmostra categoria;

    @Column(name = "massa_ou_volume", nullable = false, precision = 12, scale = 4)
    private BigDecimal massaOuVolume;

    @Column(name = "unidade_medida", nullable = false, length = 10)
    private String unidadeMedida;

    @Column(name = "data_acondicionamento", nullable = false)
    private LocalDate dataAcondicionamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicao_conservacao", nullable = false, length = 30)
    private CondicaoConservacao condicaoConservacao;

    @Column(name = "material_perigoso", nullable = false)
    private boolean materialPerigoso;

    @Lob
    @Column(name = "fotografia")
    private byte[] fotografia;

    @Column(length = 1000)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coleta_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private ColetaCientifica coleta;
}
