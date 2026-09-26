package br.edu.ifpb.pweb3.javerna.model.entity;

import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoValidacaoColeta;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "coleta_cientifica")
@Getter
@Setter
@NoArgsConstructor
public class ColetaCientifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "metodo_empregado", nullable = false, length = 150)
    private String metodoEmpregado;

    @Column(name = "descricao_ponto", nullable = false, length = 500)
    private String descricaoPonto;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperatura;

    @Column(name = "umidade_relativa", precision = 5, scale = 2)
    private BigDecimal umidadeRelativa;

    @Column(precision = 8, scale = 2)
    private BigDecimal profundidade;

    @Column(length = 1000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_validacao", nullable = false, length = 20)
    private SituacaoValidacaoColeta situacaoValidacao = SituacaoValidacaoColeta.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private Expedicao expedicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "setor_id", nullable = false)
    private SetorPesquisa setor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pesquisador_responsavel_id", nullable = false)
    private Pesquisador pesquisadorResponsavel;

    @OneToMany(
            mappedBy = "coleta",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Setter(AccessLevel.NONE)
    private List<Amostra> amostras = new ArrayList<>();

    public void adicionarAmostra(Amostra amostra) {
        Objects.requireNonNull(amostra, "A amostra e obrigatoria");
        amostras.add(amostra);
        amostra.setColeta(this);
    }

    public void removerAmostra(Amostra amostra) {
        amostras.remove(amostra);
        amostra.setColeta(null);
    }
}