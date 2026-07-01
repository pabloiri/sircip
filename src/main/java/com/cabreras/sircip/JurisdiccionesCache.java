package com.cabreras.sircip;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JurisdiccionesCache {

    private final Map<Short, Boolean> mapa = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        mapa.put((short) 901, true);
        mapa.put((short) 902, false);
        mapa.put((short) 903, true);
        mapa.put((short) 904, false);
        mapa.put((short) 905, true);
        mapa.put((short) 906, false);
        mapa.put((short) 907, true);
        mapa.put((short) 908, false);
        mapa.put((short) 909, true);
        mapa.put((short) 910, false);
        mapa.put((short) 911, true);
        mapa.put((short) 912, false);
        mapa.put((short) 913, true);
        mapa.put((short) 914, false);
        mapa.put((short) 915, true);
        mapa.put((short) 916, true);
        mapa.put((short) 917, true);
        mapa.put((short) 918, true);
        mapa.put((short) 919, true);
        mapa.put((short) 920, true);
        mapa.put((short) 921, true);
        mapa.put((short) 922, true);
        mapa.put((short) 923, true);
        mapa.put((short) 924, true);
    }

    public Boolean adheridaSircip(Short id) {
        if (id == null) return false;
        return mapa.getOrDefault(id, false);
    }
}
