package com.cabreras.sircip.repo;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JurisdiccionesCache {

    private final Map<Short, YearMonth> vigenciaDesde = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        YearMonth vigencia = YearMonth.of(2026, 9);
        vigenciaDesde.put((short) 906, vigencia);
        vigenciaDesde.put((short) 911, vigencia);
        vigenciaDesde.put((short) 913, vigencia);
        vigenciaDesde.put((short) 916, vigencia);
        vigenciaDesde.put((short) 918, vigencia);
        vigenciaDesde.put((short) 922, vigencia);
        vigenciaDesde.put((short) 923, vigencia);
    }

    public Boolean adheridaSircip(Short id, YearMonth periodo) {
        if (id == null || periodo == null) return false;
        YearMonth desde = vigenciaDesde.get(id);
        if (desde == null) return false;   // sin fecha => no adherida
        return !periodo.isBefore(desde);   // periodo >= desde
    }
}