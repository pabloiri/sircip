package com.cabreras.sircip.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PercepcionStrategyRequest(
        YearMonth periodo,
        String cuit,
        Short jurisdiccion,
        List<Short> jurisdicciones,
        BigDecimal monto
) {}
