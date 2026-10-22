package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.dto.PercepcionStrategyRequest;
import com.cabreras.sircip.entity.PadronCaba;
import com.cabreras.sircip.entity.PadronId;
import com.cabreras.sircip.repo.PadronCabaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static com.cabreras.sircip.service.PercepcionCabaStrategy.*;
import static com.cabreras.sircip.service.PercepcionCabaStrategy.CABA;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PercepcionCabaStrategyTest {

    public static final Short BSAS = 902;
    public static final String CUIT = "cuit1";
    public static final YearMonth PERIODO = YearMonth.of(2026, 11);
    public static final PadronId PADRON_ID = new PadronId(202611, CUIT);
    public static final BigDecimal BD_1000 = BigDecimal.valueOf(1000);
    public static final BigDecimal BD_100 = BigDecimal.valueOf(100);
    public static final BigDecimal ALICUOTA_PADRON = BigDecimal.valueOf(12.5);

    @InjectMocks
    private PercepcionCabaStrategy percepcionCabaStrategy;
    @Mock
    private PadronCabaRepository padronRepository;

    @Test
    @DisplayName("sin percepciones, devuelve lista vacía")
    void sinPercepciones_devuelveListaVacia() {
        PercepcionStrategyRequest request = Mockito.mock(PercepcionStrategyRequest.class);

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("cuit no inscripto en caba y no entrega en caba, no aplica y devuelve lista vacía")
    void test2() {
        PercepcionStrategyRequest request =
                new PercepcionStrategyRequest(PERIODO, CUIT, BSAS, List.of(BSAS), BD_1000);

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("cuit inscripto en caba, no entrega en caba y en padron, devuelve alicuota del padron")
    void sinPercepciones_devuelveListaVacia31() {
        PercepcionStrategyRequest request =
                new PercepcionStrategyRequest(PERIODO, CUIT, BSAS, List.of(JURISDICCION_CABA), BD_1000);
        var padron = Mockito.mock(PadronCaba.class);
        Mockito.when(padron.getAlicuota()).thenReturn(ALICUOTA_PADRON);
        Mockito.when(padronRepository.findById(PADRON_ID)).thenReturn(Optional.of(padron));

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        var importe = request.monto().multiply(ALICUOTA_PADRON).divide(BD_100, HALF_UP);
        PercepcionResponse response = new PercepcionResponse(CABA, ALICUOTA_PADRON, BD_1000, importe);
        assertThat(resultado).containsExactly(response);
    }

    @Test
    @DisplayName("cuit inscripto en caba, no entrega en caba y no en padron, devuelve alicuota del 3,5%")
    void test1() {
        PercepcionStrategyRequest request =
                new PercepcionStrategyRequest(PERIODO, CUIT, BSAS, List.of(JURISDICCION_CABA), BD_1000);
        Mockito.when(padronRepository.findById(PADRON_ID)).thenReturn(empty());

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        var importe = request.monto().multiply(ALICUOTA_1).divide(BD_100, HALF_UP);
        PercepcionResponse response = new PercepcionResponse(CABA, ALICUOTA_1, BD_1000, importe);
        assertThat(resultado).containsExactly(response);
    }

    @Test
    @DisplayName("cuit inscripto en caba, entrega en caba y no en padron, devuelve alicuota del 3,5%")
    void test5() {
        PercepcionStrategyRequest request =
                new PercepcionStrategyRequest(PERIODO, CUIT, JURISDICCION_CABA, List.of(JURISDICCION_CABA), BD_1000);
        Mockito.when(padronRepository.findById(PADRON_ID)).thenReturn(empty());

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        var importe = request.monto().multiply(ALICUOTA_1).divide(BD_100, HALF_UP);
        PercepcionResponse response = new PercepcionResponse(CABA, ALICUOTA_1, BD_1000, importe);
        assertThat(resultado).containsExactly(response);
    }

    @Test
    @DisplayName("cuit inscripto en caba, entrega en caba y en padron, devuelve alicuota del padron")
    void sinPercepciones_devuelveListaVacia32() {
        PercepcionStrategyRequest request =
                new PercepcionStrategyRequest(PERIODO, CUIT, JURISDICCION_CABA, List.of(JURISDICCION_CABA), BD_1000);
        var padron = Mockito.mock(PadronCaba.class);
        Mockito.when(padron.getAlicuota()).thenReturn(ALICUOTA_PADRON);
        Mockito.when(padronRepository.findById(PADRON_ID)).thenReturn(Optional.of(padron));

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        var importe = request.monto().multiply(ALICUOTA_PADRON).divide(BD_100, HALF_UP);
        PercepcionResponse response = new PercepcionResponse(CABA, ALICUOTA_PADRON, BD_1000, importe);
        assertThat(resultado).containsExactly(response);
    }

    @Test
    @DisplayName("cuit no inscripto en caba, entrega en caba y en padron, devuelve alicuota del padron")
    void test3() {
        PercepcionStrategyRequest request =
                new PercepcionStrategyRequest(PERIODO, CUIT, JURISDICCION_CABA, List.of(BSAS), BD_1000);
        var padron = Mockito.mock(PadronCaba.class);
        Mockito.when(padron.getAlicuota()).thenReturn(ALICUOTA_PADRON);
        Mockito.when(padronRepository.findById(PADRON_ID)).thenReturn(Optional.of(padron));

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        var importe = request.monto().multiply(ALICUOTA_PADRON).divide(BD_100, HALF_UP);
        PercepcionResponse response = new PercepcionResponse(CABA, ALICUOTA_PADRON, BD_1000, importe);
        assertThat(resultado).containsExactly(response);
    }

    @Test
    @DisplayName("cuit no inscripto en caba, entrega en caba y no en padron, devuelve 6 %")
    void test4() {
        PercepcionStrategyRequest request =
                new PercepcionStrategyRequest(PERIODO, CUIT, JURISDICCION_CABA, List.of(BSAS), BD_1000);
        Mockito.when(padronRepository.findById(PADRON_ID)).thenReturn(empty());

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        var importe = request.monto().multiply(ALICUOTA_2).divide(BD_100, HALF_UP);
        PercepcionResponse response = new PercepcionResponse(CABA, ALICUOTA_2, BD_1000, importe);
        assertThat(resultado).containsExactly(response);
    }

    @Test
    @DisplayName("cuit no esta en padron y esta inscripto en caba, devuelve 3,5 %")
    void sinPercepciones_devuelveListaVacia4() {
        PercepcionStrategyRequest request = Mockito.mock(PercepcionStrategyRequest.class);

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("cuit no esta en padron y no esta inscripto en caba, devuelve 6 %")
    void sinPercepciones_devuelveListaVacia5() {
        PercepcionStrategyRequest request = Mockito.mock(PercepcionStrategyRequest.class);

        List<PercepcionResponse> resultado = percepcionCabaStrategy.percepciones(request);
        assertThat(resultado).isEmpty();
    }

}