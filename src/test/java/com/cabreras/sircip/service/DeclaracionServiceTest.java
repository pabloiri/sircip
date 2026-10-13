package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.DeclaracionRequest;
import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.entity.Padron;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeclaracionServiceTest {

    @Mock
    private PadronService padronService;

    @Mock
    private PercepcionService percepcionService;

    @InjectMocks
    private DeclaracionService declaracionService;

    private static final String CUIT = "20123456789";
    private static final String FECHA = "15/03/2024";
    private static final YearMonth PERIODO = YearMonth.of(2024, 3);

    private DeclaracionRequest solicitud(String numeroComprobante, String monto, String alicuota, String percepcion) {
        return new DeclaracionRequest(CUIT, FECHA, "900", "1", "A", "0001",
                numeroComprobante, monto, alicuota, percepcion, "");
    }

    private DeclaracionRequest solicitudValida(String monto, String alicuota, String montoPercibido) {
        return solicitud(CUIT, FECHA, "900", monto, alicuota, montoPercibido, "00000123");
    }

    private DeclaracionRequest solicitud(String cuit, String fecha, String jurisdiccion, String monto,
                                         String alicuota, String montoPercibido, String numeroComprobante) {
        return new DeclaracionRequest(cuit, fecha, jurisdiccion, "1", "A", "0001",
                numeroComprobante, monto, alicuota, montoPercibido, "");
    }

    private void mockearPercepcionSicrip(BigDecimal alicuota, BigDecimal percepcion) {
        when(percepcionService.percepciones(any(DeclaracionRequest.class)))
                .thenReturn(List.of(new PercepcionResponse("SIRC", alicuota, null, percepcion)));
    }

    @Nested
    @DisplayName("declaracion()")
    class Declaracion {

        @Test
        @DisplayName("sin diferencias, no agrega la sección DIFERENCIAS")
        void sinDiferencias_noAgregaSeccionDiferencias() {
            mockearPercepcionSicrip(new BigDecimal("5.00"), new BigDecimal("5.00"));
            mockearPadron();
            DeclaracionRequest solicitud = solicitud("00000123", "100.00", "5.00", "5.00");

            String resultado = declaracionService.declaracion(List.of(solicitud));
            String expected = "20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,5.00,5.00,,,A";
            assertThat(resultado).isEqualTo(expected);
        }

        @Test
        @DisplayName("con diferencias, agrega la sección DIFERENCIAS con los valores calculados")
        void conDiferencias_agregaSeccionDiferencias() {
            // el request declara alicuota 5.00 / montoPercibido 5.00, pero el servicio calcula otros valores
            mockearPercepcionSicrip(new BigDecimal("7.50"), new BigDecimal("7.50"));
            mockearPadron();
            DeclaracionRequest solicitud = solicitudValida("100.00", "5.00", "5.00");

            String resultado = declaracionService.declaracion(List.of(solicitud));
            String expected = """
            20123456789,22,15/03/2024,1,,0,900,1,A,0001,00000123,100.00,5.00,5.00,,,A
            DIFERENCIAS
            20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,7.50,7.50,,,A""";
            assertThat(resultado).isEqualTo(expected);
        }

        @Test
        @DisplayName("varias lineas enviadas de distintos comprobantes sin diferencias")
        void variasSolicitudes_seUnenConSaltoDeLinea() {
            DeclaracionRequest req1 = solicitud("00000123", "100.00", "5.00", "5.00");
            DeclaracionRequest req2 = solicitud("00000124", "200.00", "1.00", "2.00");
            when(percepcionService.percepciones(req1))
                    .thenReturn(List.of(getPercepcionResponse("SIRC", "5.00", "100.00", "5.00")));
            when(percepcionService.percepciones(req2))
                    .thenReturn(List.of(getPercepcionResponse("SIRC", "1.00", "200.00", "2.00")));
            mockearPadron();

            String resultado = declaracionService.declaracion(List.of(req1, req2));
            String expected = """
            20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,5.00,5.00,,,A
            20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000124,200.00,1.00,2.00,,,A""";
            assertThat(resultado).isEqualTo(expected);
        }

        @Test
        @DisplayName("1 línea enviada y 0 calculadas: informa que no se pudo calcular la percepción")
        void unaEnviada_ceroCalculadas_noSePudoCalcular() {
            when(percepcionService.percepciones(any(DeclaracionRequest.class))).thenReturn(List.of());
            mockearPadron();
            DeclaracionRequest enviada = solicitud("00000123", "100.00", "5.00", "5.00");

            String resultado = declaracionService.declaracion(List.of(enviada));
            String expected = """
            20123456789,22,15/03/2024,1,,0,900,1,A,0001,00000123,100.00,5.00,5.00,,,A
            DIFERENCIAS
            No se pudo calcular la percepción para tipo comprobante 1, letra A, número comprobante 00000123, punto venta 0001""";
            assertThat(resultado).isEqualTo(expected);
        }

        @Test
        @DisplayName("2 líneas enviadas y 1 calculada: la enviada que sobra queda sin tipo y se informa lo calculado")
        void dosEnviadas_unaCalculada_sobraUnaEnviada() {
            when(percepcionService.percepciones(any(DeclaracionRequest.class)))
                    .thenReturn(List.of(getPercepcionResponse("SIRC", "3.00", "100.00", "3.00")));
            mockearPadron();
            DeclaracionRequest enviada1 = solicitud("00000123", "100.00", "3.00", "3.00");
            DeclaracionRequest enviada2 = solicitud("00000123", "100.00", "1.00", "1.00");

            String resultado = declaracionService.declaracion(List.of(enviada1, enviada2));
            String expected = """
            20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,3.00,3.00,,,A
            20123456789,22,15/03/2024,1,,0,900,1,A,0001,00000123,100.00,1.00,1.00,,,A
            DIFERENCIAS
            20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,3.00,3.00,,,A""";
            assertThat(resultado).isEqualTo(expected);
        }

        @Test
        @DisplayName("1 línea enviada y 2 calculadas: falta una enviada, se informa lo calculado")
        void unaEnviada_dosCalculadas_faltaUnaEnviada() {
            when(percepcionService.percepciones(any(DeclaracionRequest.class)))
                    .thenReturn(List.of(
                            getPercepcionResponse("SIRC", "3.00", "100.00", "3.00"),
                            getPercepcionResponse("SIRX", "1.00", "100.00", "1.00")));
            mockearPadron();
            DeclaracionRequest enviada = solicitud("00000123", "100.00", "3.00", "3.00");

            String resultado = declaracionService.declaracion(List.of(enviada));
            String expected = """
            20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,3.00,3.00,,,A
            DIFERENCIAS
            20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,3.00,3.00,,,A
            20123456789,22,15/03/2024,1,5,0,900,1,A,0001,00000123,100.00,1.00,1.00,,,A""";
            assertThat(resultado).isEqualTo(expected);
        }

        @Test
        @DisplayName("2 líneas enviadas y 2 calculadas con la misma alícuota: sin diferencias")
        void dosEnviadas_dosCalculadas_mismasAlicuotas_sinDiferencias() {
            when(percepcionService.percepciones(any(DeclaracionRequest.class)))
                    .thenReturn(List.of(
                            getPercepcionResponse("SIRC", "1.00", "100.00", "1.00"),
                            getPercepcionResponse("SIRX", "1.00", "100.00", "1.00")));
            mockearPadron();
            DeclaracionRequest enviada1 = solicitud("00000123", "100.00", "1.00", "1.00");
            DeclaracionRequest enviada2 = solicitud("00000123", "100.00", "1.00", "1.00");

            String resultado = declaracionService.declaracion(List.of(enviada1, enviada2));
            String expected = """
            20123456789,22,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,1.00,1.00,,,A
            20123456789,22,15/03/2024,1,5,0,900,1,A,0001,00000123,100.00,1.00,1.00,,,A""";
            assertThat(resultado).isEqualTo(expected);
            assertThat(resultado).doesNotContain("DIFERENCIAS");
        }

        private void mockearPadron() {
            Padron padron = mock(Padron.class);
            when(padron.getCrc()).thenReturn((short) 22);
            when(padronService.getPadron(PERIODO, CUIT)).thenReturn(Optional.of(padron));
        }

        private static PercepcionResponse getPercepcionResponse(String codigoImpuesto, String alicuota, String baseImponible, String percepcion) {
            return new PercepcionResponse(codigoImpuesto,
                    new BigDecimal(alicuota), new BigDecimal(baseImponible), new BigDecimal(percepcion));
        }

    }

    @Nested
    @DisplayName("hayInvalidas()")
    class HayInvalidas {

        @Test
        @DisplayName("lista vacía no tiene inválidas")
        void listaVacia_devuelveFalse() {
            assertThat(declaracionService.hayInvalidas(List.of())).isFalse();
        }

        @Test
        @DisplayName("solicitud con todos los datos válidos no es inválida")
        void datosValidos_devuelveFalse() {
            DeclaracionRequest solicitud = solicitudValida("100.00", "5.00", "5.00");
            assertThat(declaracionService.hayInvalidas(List.of(solicitud))).isFalse();
        }

        @Test
        @DisplayName("fecha con formato incorrecto es inválida")
        void fechaInvalida_devuelveTrue() {
            DeclaracionRequest req = solicitud(CUIT, "2024-03-15", "900", "100.00", "5.00", "5.00", "00000123");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("jurisdicción no numérica es inválida")
        void jurisdiccionInvalida_devuelveTrue() {
            DeclaracionRequest req = solicitud(CUIT, FECHA, "abc", "100.00", "5.00", "5.00", "00000123");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("monto no numérico es inválido")
        void montoInvalido_devuelveTrue() {
            DeclaracionRequest req = solicitudValida("cien", "5.00", "5.00");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("alícuota no numérica es inválida")
        void alicuotaInvalida_devuelveTrue() {
            DeclaracionRequest req = solicitudValida("100.00", "cinco", "5.00");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("montoPercibido no numérico es inválido")
        void montoPercibidoInvalido_devuelveTrue() {
            DeclaracionRequest req = solicitudValida("100.00", "5.00", "cinco");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("una sola solicitud inválida entre varias válidas alcanza para devolver true")
        void unaInvalidaEntreVarias_devuelveTrue() {
            DeclaracionRequest valida = solicitudValida("100.00", "5.00", "5.00");
            DeclaracionRequest invalida = solicitud(CUIT, "fecha-mala", "900", "100.00", "5.00", "5.00", "00000123");

            assertThat(declaracionService.hayInvalidas(List.of(valida, invalida))).isTrue();
        }

    }
}