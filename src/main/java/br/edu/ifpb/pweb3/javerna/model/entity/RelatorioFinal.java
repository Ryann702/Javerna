package br.edu.ifpb.pweb3.javerna.model.entity;

import br.edu.ifpb.pweb3.javerna.model.enums.SituacaoRelatorio;
import jakarta.persistence.Basic;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "relatorio_final")
@Getter
@Setter
@NoArgsConstructor
public class RelatorioFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String resumo;

    @Column(name = "data_submissao", nullable = false)
    private LocalDate dataSubmissao;

    @Column(name = "numero_total_paginas", nullable = false)
    private Integer numeroTotalPaginas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoRelatorio situacao;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "arquivo_completo", nullable = false)
    private byte[] arquivoCompleto;

    @Column(name = "publicacao_autorizada", nullable = false)
    private boolean publicacaoAutorizada;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false, unique = true)
    @Setter(AccessLevel.PACKAGE)
    private Expedicao expedicao;
}
