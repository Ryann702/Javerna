package br.edu.ifpb.pweb3.javerna.model.entity;

import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoExpedicao;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "expedicao")
@Getter
@Setter
@NoArgsConstructor
public class Expedicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String objetivo;

    @Column(name = "inicio_previsto", nullable = false)
    private LocalDateTime inicioPrevisto;

    @Column(name = "termino_previsto", nullable = false)
    private LocalDateTime terminoPrevisto;

    @Column(name = "orcamento_aprovado", nullable = false, precision = 14, scale = 2)
    private BigDecimal orcamentoAprovado;

    @Column(name = "custo_realizado", nullable = false, precision = 14, scale = 2)
    private BigDecimal custoRealizado = BigDecimal.ZERO;

    @Column(name = "quantidade_maxima_participantes", nullable = false)
    private Integer quantidadeMaximaParticipantes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoExpedicao situacao = SituacaoExpedicao.PLANEJADA;

    @Column(name = "cancelamento_emergencial", nullable = false)
    private boolean cancelamentoEmergencial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caverna_id", nullable = false)
    private Caverna caverna;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "expedicao_setor",
            joinColumns = @JoinColumn(name = "expedicao_id"),
            inverseJoinColumns = @JoinColumn(name = "setor_id")
    )
    @Setter(AccessLevel.NONE)
    private Set<SetorPesquisa> setores = new HashSet<>();

    @OneToOne(
            mappedBy = "expedicao",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            optional = false
    )
    @Setter(AccessLevel.NONE)
    private PlanoSeguranca planoSeguranca;

    @OneToOne(
            mappedBy = "expedicao",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Setter(AccessLevel.NONE)
    private AutorizacaoAmbiental autorizacaoAmbiental;

    @OneToOne(
            mappedBy = "expedicao",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Setter(AccessLevel.NONE)
    private RelatorioFinal relatorioFinal;

    @OneToMany(
            mappedBy = "expedicao",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Setter(AccessLevel.NONE)
    private List<ParticipacaoExpedicao> participacoes = new ArrayList<>();

    public void adicionarSetor(SetorPesquisa setor) {
        Objects.requireNonNull(setor, "O setor e obrigatorio");

        if (!pertenceAMesmaCaverna(setor)) {
            throw new IllegalArgumentException("O setor deve pertencer a caverna da expedicao");
        }

        setores.add(setor);
        setor.vincularExpedicao(this);
    }

    public void removerSetor(SetorPesquisa setor) {
        setores.remove(setor);
        setor.desvincularExpedicao(this);
    }

    public void definirPlanoSeguranca(PlanoSeguranca planoSeguranca) {
        Objects.requireNonNull(planoSeguranca, "O plano de seguranca e obrigatorio");
        this.planoSeguranca = planoSeguranca;
        planoSeguranca.setExpedicao(this);
    }

    public void definirAutorizacaoAmbiental(AutorizacaoAmbiental autorizacaoAmbiental) {
        this.autorizacaoAmbiental = autorizacaoAmbiental;
        if (autorizacaoAmbiental != null) {
            autorizacaoAmbiental.setExpedicao(this);
        }
    }

    public void definirRelatorioFinal(RelatorioFinal relatorioFinal) {
        this.relatorioFinal = relatorioFinal;
        if (relatorioFinal != null) {
            relatorioFinal.setExpedicao(this);
        }
    }

    public void adicionarParticipacao(ParticipacaoExpedicao participacao) {
        Objects.requireNonNull(participacao, "A participacao e obrigatoria");
        Objects.requireNonNull(participacao.getPessoa(), "A pessoa participante e obrigatoria");

        if (participacoes.stream().anyMatch(atual -> mesmaPessoa(
                atual.getPessoa(), participacao.getPessoa()))) {
            throw new IllegalArgumentException("A pessoa ja participa desta expedicao");
        }

        participacoes.add(participacao);
        participacao.setExpedicao(this);
        participacao.getPessoa().vincularParticipacao(participacao);
    }

    public void removerParticipacao(ParticipacaoExpedicao participacao) {
        participacoes.remove(participacao);
        participacao.getPessoa().desvincularParticipacao(participacao);
        participacao.setExpedicao(null);
    }

    private boolean pertenceAMesmaCaverna(SetorPesquisa setor) {
        if (caverna == setor.getCaverna()) {
            return true;
        }

        return caverna != null
                && setor.getCaverna() != null
                && caverna.getId() != null
                && caverna.getId().equals(setor.getCaverna().getId());
    }

    private boolean mesmaPessoa(Pessoa primeira, Pessoa segunda) {
        if (primeira == segunda) {
            return true;
        }

        return primeira != null
                && segunda != null
                && primeira.getId() != null
                && primeira.getId().equals(segunda.getId());
    }

    @PrePersist
    @PreUpdate
    private void validarPlanoSeguranca() {
        if (planoSeguranca == null) {
            throw new IllegalStateException("A expedicao deve possuir um plano de seguranca");
        }
    }
}
