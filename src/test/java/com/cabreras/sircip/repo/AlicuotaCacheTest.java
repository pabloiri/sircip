package com.cabreras.sircip.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class AlicuotaCacheTest {

    private AlicuotaCache cache;

    @BeforeEach
    void setUp() {
        cache = new AlicuotaCache();
        cache.init();
    }

    @Test
    @DisplayName("Debe retornar ZERO si la letra de la alícuota es null")
    void obtenerPorcentaje_CuandoLetraEsNull_RetornaZero() {
        assertEquals(AlicuotaCache.ZERO, cache.obtenerPorcentaje(null));
    }

    @Test
    @DisplayName("Debe retornar ZERO si la letra de la alícuota no existe en el mapa")
    void obtenerPorcentaje_CuandoLetraNoExiste_RetornaZero() {
        assertEquals(AlicuotaCache.ZERO, cache.obtenerPorcentaje("Z"));
    }

    @ParameterizedTest
    @CsvSource({
            "A, 350",
            "B, 250",
            "E, 100",
            "F, 0",
            "G, 420",
            "H, 500"
    })
    @DisplayName("Debe retornar el valor correcto en escala 2 para letras válidas en mayúscula")
    void obtenerPorcentaje_CuandoLetraExisteEnMayuscula_RetornaValorEscalaDos(String letra, long valorEsperado) {
        assertEquals(valorEsperado, cache.obtenerPorcentaje(letra));
    }

    @ParameterizedTest
    @CsvSource({
            "a, 350",
            "b, 250",
            "e, 100",
            "f, 0",
            "g, 420",
            "h, 500"
    })
    @DisplayName("Debe retornar el valor correcto en escala 2 aunque la letra se envíe en minúscula")
    void obtenerPorcentaje_CuandoLetraExisteEnMinuscula_RetornaValorEscalaDos(String letra, long valorEsperado) {
        assertEquals(valorEsperado, cache.obtenerPorcentaje(letra));
    }
}
