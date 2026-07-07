package com.cabreras.sircip;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.RoundingMode.*;

@Component
public class AlicuotaCache {

    public static final BigDecimal ZERO = BigDecimal.valueOf(0.00).setScale(2, HALF_UP);

    private final Map<String, BigDecimal> mapa = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        mapa.put("A", BigDecimal.valueOf(3.50).setScale(2, HALF_UP));
        mapa.put("B", BigDecimal.valueOf(2.50).setScale(2, HALF_UP));
        mapa.put("E", BigDecimal.valueOf(1.00).setScale(2, HALF_UP));
        mapa.put("F", BigDecimal.valueOf(0.00).setScale(2, HALF_UP));
        mapa.put("G", BigDecimal.valueOf(4.20).setScale(2, HALF_UP));
        mapa.put("H", BigDecimal.valueOf(5.00).setScale(2, HALF_UP));
    }

    public BigDecimal obtenerPorcentaje(String letra) {
        if (letra == null) return ZERO;
        return mapa.getOrDefault(letra.toUpperCase(), ZERO);
    }
}
