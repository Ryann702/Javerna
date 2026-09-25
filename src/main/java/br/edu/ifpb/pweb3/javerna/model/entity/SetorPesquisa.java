package br.edu.ifpb.pweb3.javerna.model.entity;

import br.edu.ifpb.pweb3.javerna.model.enums.NivelDificuldade;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "setor_pesquisa")
@Getter
@Setter
@NoArgsConstructor
public class SetorPesquisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String denominacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_dificuldade", nullable = false, length = 20)
    private NivelDificuldade nivelDificuldade;

    @Column(name = "profundidade_maxima", nullable = false, precision = 10, scale = 2)
    private BigDecimal profundidadeMaxima;

    @Column(name = "extensao_aproximada", nullable = false, precision = 12, scale = 2)
    private BigDecimal extensaoAproximada;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "risco_inundacao", nullable = false)
    private boolean riscoInundacao;

    @Column(name = "condicao_atual", nullable = false, length = 100)
    private String condicaoAtual;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caverna_id", nullable = false)
    private Caverna caverna;
}
