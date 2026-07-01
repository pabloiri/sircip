package com.cabreras.sircip;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record PadronResponse(
        String codigoImpuesto,
        BigDecimal alicuota,
        BigDecimal baseImponible,
        BigDecimal importe
) {}
