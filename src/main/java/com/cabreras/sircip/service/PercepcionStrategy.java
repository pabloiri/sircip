package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.dto.PercepcionStrategyRequest;

import java.math.BigDecimal;
import java.util.List;

public interface PercepcionStrategy {

    BigDecimal BD_100 = new BigDecimal("100.00");

    List<PercepcionResponse> percepciones(PercepcionStrategyRequest request);
}
