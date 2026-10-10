package com.cabreras.sircip.repo;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlicuotaCache {

    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private final Map<String, BigDecimal> mapa = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Valores en porcentaje (2.00 = 2%)
        mapa.put("A", new BigDecimal("0.00"));
        mapa.put("B", new BigDecimal("0.01"));
        mapa.put("C", new BigDecimal("0.05"));
        mapa.put("D", new BigDecimal("0.10"));
        mapa.put("E", new BigDecimal("0.20"));
        mapa.put("F", new BigDecimal("0.30"));
        mapa.put("G", new BigDecimal("0.40"));
        mapa.put("H", new BigDecimal("0.50"));
        mapa.put("I", new BigDecimal("0.60"));
        mapa.put("J", new BigDecimal("0.70"));
        mapa.put("K", new BigDecimal("0.80"));
        mapa.put("L", new BigDecimal("1.00"));
        mapa.put("M", new BigDecimal("1.20"));
        mapa.put("N", new BigDecimal("1.40"));
        mapa.put("O", new BigDecimal("1.50"));
        mapa.put("P", new BigDecimal("1.60"));
        mapa.put("Q", new BigDecimal("1.80"));
        mapa.put("R", new BigDecimal("2.00"));
        mapa.put("S", new BigDecimal("2.50"));
        mapa.put("T", new BigDecimal("3.00"));
        mapa.put("U", new BigDecimal("3.50"));
        mapa.put("V", new BigDecimal("4.00"));
        mapa.put("W", new BigDecimal("4.50"));
        mapa.put("X", new BigDecimal("5.00"));
    }

    /** Devuelve el porcentaje (por ejemplo 2.00 para el 2%). Nunca devuelve null. */
    public BigDecimal obtenerPorcentaje(String letra) {
        if (letra == null) return ZERO;
        return mapa.getOrDefault(letra.toUpperCase(), ZERO);
    }
}