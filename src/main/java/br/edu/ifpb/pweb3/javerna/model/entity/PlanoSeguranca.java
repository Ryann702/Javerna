package br.edu.ifpb.pweb3.javerna.model.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plano_seguranca")
@Getter
@Setter
@NoArgsConstructor
public class PlanoSeguranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "procedimentos_evacuacao", nullable = false, length = 2000)
    private String procedimentosEvacuacao;

    @Column(name = "ponto_externo_encontro", nullable = false, length = 200)
    private String pontoExternoEncontro;

    @Column(name = "tempo_maximo_sem_comunicacao", nullable = false)
    private Integer tempoMaximoSemComunicacao;

    @Column(name = "telefone_emergencia", nullable = false, length = 20)
    private String telefoneEmergencia;

    @Column(name = "necessita_equipe_medica", nullable = false)
    private boolean necessitaEquipeMedica;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "mapa_rota", nullable = false)
    private byte[] mapaRota;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false, unique = true)
    @Setter(AccessLevel.PACKAGE)
    private Expedicao expedicao;
}
