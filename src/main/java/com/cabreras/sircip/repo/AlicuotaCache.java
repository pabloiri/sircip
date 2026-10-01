package com.cabreras.sircip.repo;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlicuotaCache {

    // Escala 2: todos los valores se almacenan multiplicados por 100
    public static final long ZERO = 0L;

    private final Map<String, Long> mapa = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Los valores se almacenan en escala 2 (multiplicados por 100)
        mapa.put("A", 0L);
        mapa.put("B", 1L);
        mapa.put("C", 5L);
        mapa.put("D", 10L);
        mapa.put("E", 20L);
        mapa.put("F", 30L);
        mapa.put("G", 40L);
        mapa.put("H", 50L);
        mapa.put("I", 60L);
        mapa.put("J", 70L);
        mapa.put("K", 80L);
        mapa.put("L", 100L);
        mapa.put("M", 120L);
        mapa.put("N", 140L);
        mapa.put("O", 150L);
        mapa.put("P", 160L);
        mapa.put("Q", 180L);
        mapa.put("R", 200L);
        mapa.put("S", 250L);
        mapa.put("T", 300L);
        mapa.put("U", 350L);
        mapa.put("V", 400L);
        mapa.put("W", 450L);
        mapa.put("X", 500L);
    }

    /**
     * Devuelve el porcentaje en escala 2 (multiplicado por 100)
     */
    public long obtenerPorcentaje(String letra) {
        if (letra == null) return ZERO;
        return mapa.getOrDefault(letra.toUpperCase(), ZERO);
    }
}