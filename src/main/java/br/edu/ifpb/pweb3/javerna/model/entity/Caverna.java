package br.edu.ifpb.pweb3.javerna.model.entity;

import br.edu.ifpb.pweb3.javerna.model.embeddable.Localizacao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
@Table(name = "caverna")
@Getter
@Setter
@NoArgsConstructor
public class Caverna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_oficial", nullable = false, length = 120)
    private String nomeOficial;

    @Column(name = "codigo_ambiental", nullable = false, unique = true, length = 40)
    private String codigoAmbiental;

    @Column(nullable = false, length = 100)
    private String municipio;

    @Column(name = "uf", nullable = false, length = 2)
    private String unidadeFederativa;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal altitude;

    @Column(name = "extensao_conhecida", nullable = false, precision = 12, scale = 2)
    private BigDecimal extensaoConhecida;

    @Column(name = "data_ultima_inspecao", nullable = false)
    private LocalDate dataUltimaInspecao;

    @Column(name = "acesso_permitido", nullable = false)
    private boolean acessoPermitido;

    @Embedded
    private Localizacao localizacao;

    @OneToMany(
            mappedBy = "caverna",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Setter(AccessLevel.NONE)
    private List<SetorPesquisa> setores = new ArrayList<>();

    public void adicionarSetor(SetorPesquisa setor) {
        setores.add(setor);
        setor.setCaverna(this);
    }

    public void removerSetor(SetorPesquisa setor) {
        setores.remove(setor);
        setor.setCaverna(null);
    }
}
