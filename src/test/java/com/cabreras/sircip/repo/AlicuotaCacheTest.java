package com.cabreras.sircip.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

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
    @CsvSource({"A, 0.00", "B, 0.01", "C, 0.05", "D, 0.10", "E, 0.20", "F, 0.30", "G, 0.40", "H, 0.50",
            "I, 0.60", "J, 0.70", "K, 0.80", "L, 1.00", "M, 1.20", "N, 1.40", "O, 1.50", "P, 1.60",
            "Q, 1.80", "R, 2.00", "S, 2.50", "T, 3.00", "U, 3.50", "V, 4.00", "W, 4.50", "X, 5.00"})
    @DisplayName("Debe retornar el porcentaje correcto para letras válidas en mayúscula")
    void obtenerPorcentaje_CuandoLetraExisteEnMayuscula_RetornaPorcentaje(String letra, BigDecimal valorEsperado) {
        assertEquals(valorEsperado, cache.obtenerPorcentaje(letra));
    }

    @ParameterizedTest
    @CsvSource({"a, 0.00", "b, 0.01", "e, 0.20", "f, 0.30", "g, 0.40", "h, 0.50"})
    @DisplayName("Debe retornar el porcentaje correcto aunque la letra se envíe en minúscula")
    void obtenerPorcentaje_CuandoLetraExisteEnMinuscula_RetornaPorcentaje(String letra, BigDecimal valorEsperado) {
        assertEquals(valorEsperado, cache.obtenerPorcentaje(letra));
    }
}