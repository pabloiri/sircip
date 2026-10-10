package com.cabreras.sircip.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class JurisdiccionesCacheTest {

    private static final YearMonth VIGENCIA = YearMonth.of(2026, 10);
    private static final YearMonth ANTERIOR = VIGENCIA.minusMonths(1);
    private static final YearMonth POSTERIOR = VIGENCIA.plusMonths(1);

    private JurisdiccionesCache jurisdiccionesCache;

    @BeforeEach
    void setUp() {
        jurisdiccionesCache = new JurisdiccionesCache();
        jurisdiccionesCache.init();
    }

    @Test
    @DisplayName("Debe retornar false si el ID de la jurisdicción es null")
    void adheridaSircip_CuandoIdEsNull_RetornaFalse() {
        assertFalse(jurisdiccionesCache.adheridaSircip(null, VIGENCIA));
    }

    @Test
    @DisplayName("Debe retornar false si el periodo es null")
    void adheridaSircip_CuandoPeriodoEsNull_RetornaFalse() {
        assertFalse(jurisdiccionesCache.adheridaSircip((short) 901, null));
    }

    @Test
    @DisplayName("Debe retornar false si la jurisdicción no existe en el mapa")
    void adheridaSircip_CuandoIdNoExiste_RetornaFalse() {
        assertFalse(jurisdiccionesCache.adheridaSircip((short) 999, VIGENCIA));
    }

    @ParameterizedTest
    @ValueSource(shorts = {901, 903, 905, 907, 909, 911, 913, 915, 916, 917, 918, 919, 920, 921, 922, 923, 924})
    @DisplayName("Debe retornar true si el periodo es igual a la vigencia desde")
    void adheridaSircip_CuandoPeriodoIgualAVigencia_RetornaTrue(short id) {
        assertTrue(jurisdiccionesCache.adheridaSircip(id, VIGENCIA));
    }

    @ParameterizedTest
    @ValueSource(shorts = {901, 903, 905, 907, 909, 911, 913, 915, 916, 917, 918, 919, 920, 921, 922, 923, 924})
    @DisplayName("Debe retornar true si el periodo es posterior a la vigencia desde")
    void adheridaSircip_CuandoPeriodoPosteriorAVigencia_RetornaTrue(short id) {
        assertTrue(jurisdiccionesCache.adheridaSircip(id, POSTERIOR));
    }

    @ParameterizedTest
    @ValueSource(shorts = {901, 903, 905, 907, 909, 911, 913, 915, 916, 917, 918, 919, 920, 921, 922, 923, 924})
    @DisplayName("Debe retornar false si el periodo es anterior a la vigencia desde")
    void adheridaSircip_CuandoPeriodoAnteriorAVigencia_RetornaFalse(short id) {
        assertFalse(jurisdiccionesCache.adheridaSircip(id, ANTERIOR));
    }

    @ParameterizedTest
    @ValueSource(shorts = {902, 904, 906, 908, 910, 912, 914})
    @DisplayName("Debe retornar false para las jurisdicciones sin fecha de vigencia, sin importar el periodo")
    void adheridaSircip_CuandoJurisdiccionNoEstaAdherida_RetornaFalse(short id) {
        assertFalse(jurisdiccionesCache.adheridaSircip(id, ANTERIOR));
        assertFalse(jurisdiccionesCache.adheridaSircip(id, VIGENCIA));
        assertFalse(jurisdiccionesCache.adheridaSircip(id, POSTERIOR));
    }
}