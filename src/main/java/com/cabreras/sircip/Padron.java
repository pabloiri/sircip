package com.cabreras.sircip;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigInteger;

@Entity
@Table(name = "padron")
@Getter
public class Padron {

    @EmbeddedId
    private PadronId id;

    @Column(name = "razon_social", length = 70)
    private String razonSocial;
    private Short jurisdiccion;
    private Short crc;
    @Column(name = "letra_alicuota", length = 1)
    private String letraAlicuota;
    @Column(name = "campo7", precision = 25)
    private BigInteger campo7;

}

