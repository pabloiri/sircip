package com.cabreras.sircip;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlicuotaCache {

    private final Map<String, Double> mapa = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        mapa.put("A", 3.50);
        mapa.put("B", 2.50);
        mapa.put("E", 1.00);
        mapa.put("F", 0.00);
        mapa.put("G", 4.20);
        mapa.put("H", 5.00);
    }

    public Double obtenerPorcentaje(String letra) {
        if (letra == null) return 0.0;
        return mapa.getOrDefault(letra.toUpperCase(), 0.0);
    }
}
