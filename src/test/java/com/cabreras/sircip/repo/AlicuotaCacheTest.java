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
    @CsvSource({"A, 0", "B, 1", "C, 5", "D, 10", "E, 20", "F, 30", "G, 40", "H, 50", "I, 60", "J, 70", "K, 80",
            "L, 100", "M, 120", "N, 140", "O, 150", "P, 160", "Q, 180", "R, 200", "S, 250", "T, 300", "U, 350",
            "V, 400", "W, 450", "X, 500"})
    @DisplayName("Debe retornar el valor correcto en escala 2 para letras válidas en mayúscula")
    void obtenerPorcentaje_CuandoLetraExisteEnMayuscula_RetornaValorEscalaDos(String letra, long valorEsperado) {
        assertEquals(valorEsperado, cache.obtenerPorcentaje(letra));
    }

    @ParameterizedTest
    @CsvSource({"a, 0", "b, 1", "e, 20", "f, 30", "g, 40", "h, 50"})
    @DisplayName("Debe retornar el valor correcto en escala 2 aunque la letra se envíe en minúscula")
    void obtenerPorcentaje_CuandoLetraExisteEnMinuscula_RetornaValorEscalaDos(String letra, long valorEsperado) {
        assertEquals(valorEsperado, cache.obtenerPorcentaje(letra));
    }
}
