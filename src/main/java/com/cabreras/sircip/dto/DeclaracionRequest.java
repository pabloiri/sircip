package com.cabreras.sircip.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeclaracionRequest(
        String cuit,
        String fecha,
        String jurisdiccion,
        String tipoComprobante,
        String letra,
        String puntoVenta,
        String numeroComprobante,
        String monto,
        String alicuota,
        String montoPercibido,
        String comprobanteOriginal
) {}