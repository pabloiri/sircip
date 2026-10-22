package com.cabreras.sircip.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.math.BigDecimal;

@MappedSuperclass
@Getter
public abstract class PadronAlicuota extends PadronBase {

    private BigDecimal alicuota;

}