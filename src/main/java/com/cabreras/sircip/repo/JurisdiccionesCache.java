package com.cabreras.sircip.repo;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.time.YearMonth;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JurisdiccionesCache {

    // jurisdiccion -> periodo desde el cual está adherida a SIRCIP
    private final Map<Short, YearMonth> vigenciaDesde = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        vigenciaDesde.put((short) 901, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 903, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 905, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 907, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 909, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 911, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 913, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 915, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 916, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 917, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 918, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 919, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 920, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 921, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 922, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 923, YearMonth.of(2026, 12));
        vigenciaDesde.put((short) 924, YearMonth.of(2026, 12));
    }

    public Boolean adheridaSircip(Short id, YearMonth periodo) {
        if (id == null || periodo == null) return false;
        YearMonth desde = vigenciaDesde.get(id);
        if (desde == null) return false;   // sin fecha => no adherida
        return !periodo.isBefore(desde);   // periodo >= desde
    }
}