package com.cabreras.sircip;

import java.math.BigDecimal;

public record PadronResponse(
        String codigoImpuesto,
        BigDecimal alicuota,
        BigDecimal baseImponible,
        BigDecimal importe
) {}
