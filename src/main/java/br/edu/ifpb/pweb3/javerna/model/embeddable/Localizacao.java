package br.edu.ifpb.pweb3.javerna.model.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Localizacao {

    @Column(name = "localizacao_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "localizacao_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "localizacao_datum", nullable = false, length = 30)
    private String datumGeodesico;
}
