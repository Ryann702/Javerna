package br.edu.ifpb.pweb3.javerna.model.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Column(name = "endereco_logradouro", nullable = false, length = 120)
    private String logradouro;

    @Column(name = "endereco_numero", nullable = false, length = 10)
    private String numero;

    @Column(name = "endereco_complemento", length = 100)
    private String complemento;

    @Column(name = "endereco_bairro", nullable = false, length = 80)
    private String bairro;

    @Column(name = "endereco_cidade", nullable = false, length = 80)
    private String cidade;

    @Column(name = "endereco_uf", nullable = false, length = 2)
    private String unidadeFederativa;

    @Column(name = "endereco_cep", nullable = false, length = 8)
    private String cep;
}
