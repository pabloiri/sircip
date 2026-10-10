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
        // TODO : cambiar la vigencia
        YearMonth vigencia = YearMonth.of(2026, 10);
        vigenciaDesde.put((short) 901, vigencia);
        vigenciaDesde.put((short) 903, vigencia);
        vigenciaDesde.put((short) 905, vigencia);
        vigenciaDesde.put((short) 907, vigencia);
        vigenciaDesde.put((short) 909, vigencia);
        vigenciaDesde.put((short) 911, vigencia);
        vigenciaDesde.put((short) 913, vigencia);
        vigenciaDesde.put((short) 915, vigencia);
        vigenciaDesde.put((short) 916, vigencia);
        vigenciaDesde.put((short) 917, vigencia);
        vigenciaDesde.put((short) 918, vigencia);
        vigenciaDesde.put((short) 919, vigencia);
        vigenciaDesde.put((short) 920, vigencia);
        vigenciaDesde.put((short) 921, vigencia);
        vigenciaDesde.put((short) 922, vigencia);
        vigenciaDesde.put((short) 923, vigencia);
        vigenciaDesde.put((short) 924, vigencia);
    }

    public Boolean adheridaSircip(Short id, YearMonth periodo) {
        if (id == null || periodo == null) return false;
        YearMonth desde = vigenciaDesde.get(id);
        if (desde == null) return false;   // sin fecha => no adherida
        return !periodo.isBefore(desde);   // periodo >= desde
    }
}