package br.edu.ifpb.pweb3.javerna.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pesquisador")
@PrimaryKeyJoinColumn(name = "pessoa_id")
@Getter
@Setter
@NoArgsConstructor
public class Pesquisador extends Pessoa {

    @Column(name = "registro_institucional", nullable = false, unique = true, length = 30)
    private String registroInstitucional;

    @Column(name = "area_principal_pesquisa", nullable = false, length = 100)
    private String areaPrincipalPesquisa;

    @Column(nullable = false, length = 80)
    private String titulacao;

    @Column(name = "valor_diario_bolsa", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorDiarioBolsa;
}
