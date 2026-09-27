package com.cabreras.sircip.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class JurisdiccionesCacheTest {

    private JurisdiccionesCache jurisdiccionesCache;

    @BeforeEach
    void setUp() {
        jurisdiccionesCache = new JurisdiccionesCache();
        jurisdiccionesCache.init();
    }

    @Test
    void adheridaSircip() {
        assertTrue(jurisdiccionesCache.adheridaSircip((short) 901));
    }

    @Test
    @DisplayName("Debe retornar false si el ID de la jurisdicción es null")
    void adheridaSircip_CuandoIdEsNull_RetornaFalse() {
        assertFalse(jurisdiccionesCache.adheridaSircip(null));
    }

    @Test
    @DisplayName("Debe retornar false si la jurisdicción no existe en el mapa")
    void adheridaSircip_CuandoIdNoExiste_RetornaFalse() {
        assertFalse(jurisdiccionesCache.adheridaSircip((short) 999));
    }

    @ParameterizedTest
    @ValueSource(shorts = {901, 903, 905, 907, 909, 911, 913, 915, 916, 917, 918, 919, 920, 921, 922, 923, 924})
    @DisplayName("Debe retornar true para las jurisdicciones adheridas configuradas en true")
    void adheridaSircip_CuandoJurisdiccionEstaAdherida_RetornaTrue(short id) {
        assertTrue(jurisdiccionesCache.adheridaSircip(id));
    }

    @ParameterizedTest
    @ValueSource(shorts = {902, 904, 906, 908, 910, 912, 914})
    @DisplayName("Debe retornar false para las jurisdicciones configuradas explícitamente en false")
    void adheridaSircip_CuandoJurisdiccionNoEstaAdherida_RetornaFalse(short id) {
        assertFalse(jurisdiccionesCache.adheridaSircip(id));
    }

}