package com.cabreras.sircip;

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
        mapa.put("A", 350L);
        mapa.put("B", 250L);
        mapa.put("E", 100L);
        mapa.put("F", 0L);
        mapa.put("G", 420L);
        mapa.put("H", 500L);
    }

    /**
     * Devuelve el porcentaje en escala 2 (multiplicado por 100)
     */
    public long obtenerPorcentaje(String letra) {
        if (letra == null) return ZERO;
        return mapa.getOrDefault(letra.toUpperCase(), ZERO);
    }
}