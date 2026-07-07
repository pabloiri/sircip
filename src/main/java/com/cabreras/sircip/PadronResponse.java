package com.cabreras.sircip;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PadronResponse(
        String codigoImpuesto,
        BigDecimal alicuota,
        BigDecimal baseImponible,
        BigDecimal importe
) {}
