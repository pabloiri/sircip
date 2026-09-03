package com.cabreras.sircip.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PercepcionResponse(
        String codigoImpuesto,
        BigDecimal alicuota,
        BigDecimal baseImponible,
        BigDecimal importe
) {}
