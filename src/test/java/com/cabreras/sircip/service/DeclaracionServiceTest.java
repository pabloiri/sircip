package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.DeclaracionRequest;
import com.cabreras.sircip.dto.PercepcionTotal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    private DeclaracionRequest solicitudValida() {
        return solicitud(CUIT, FECHA, "900", "100.00", "5.00", "5.00");
    }

    private DeclaracionRequest solicitud(String cuit, String fecha, String jurisdiccion,
                                         String monto, String alicuota, String montoPercibido) {
        return new DeclaracionRequest(cuit, fecha, jurisdiccion, "1", "A", "0001",
                "00000123", monto, alicuota, montoPercibido, "");
    }

    private void mockearPercepcionSicrip(BigDecimal alicuota, BigDecimal percepcion) {
        when(percepcionService.percepcionSircip(any(DeclaracionRequest.class)))
                .thenReturn(new PercepcionTotal(alicuota, percepcion));
    }

    @Nested
    @DisplayName("declaracion()")
    class Declaracion {

        @Test
        @DisplayName("sin diferencias, no agrega la sección DIFERENCIAS")
        void sinDiferencias_noAgregaSeccionDiferencias() {
            mockearPercepcionSicrip(new BigDecimal("5.00"), new BigDecimal("5.00"));
            when(padronService.obtenerCrc(CUIT, FECHA)).thenReturn("X");
            String expected = "20123456789,X,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,5.00,5.00,,,A";

            String resultado = declaracionService.declaracion(List.of(solicitudValida()));

            assertThat(resultado).isEqualTo(expected);
        }

        @Test
        @DisplayName("con diferencias, agrega la sección DIFERENCIAS con los valores calculados")
        void conDiferencias_agregaSeccionDiferencias() {
            // el request declara alicuota 5.00 / montoPercibido 5.00, pero el servicio calcula otros valores
            mockearPercepcionSicrip(new BigDecimal("7.50"), new BigDecimal("7.50"));
            when(padronService.obtenerCrc(CUIT, FECHA)).thenReturn("Y");
            String expected = """
            20123456789,Y,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,5.00,5.00,,,A
            DIFERENCIAS
            20123456789,Y,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,7.50,7.50,,,A""";

            String resultado = declaracionService.declaracion(List.of(solicitudValida()));

            assertThat(resultado).isEqualTo(expected);
        }

        @Test
        @DisplayName("varias solicitudes se unen con salto de línea")
        void variasSolicitudes_seUnenConSaltoDeLinea() {
            mockearPercepcionSicrip(new BigDecimal("5.00"), new BigDecimal("5.00"));
            when(padronService.obtenerCrc(CUIT, FECHA)).thenReturn("X");
            DeclaracionRequest req1 = solicitudValida();
            DeclaracionRequest req2 = solicitudValida();

            String resultado = declaracionService.declaracion(List.of(req1, req2));

            // TODO : usar distintos requests
            String expected = """
                    20123456789,X,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,5.00,5.00,,,A
                    20123456789,X,15/03/2024,1,1,0,900,1,A,0001,00000123,100.00,5.00,5.00,,,A""";
            assertThat(resultado).isEqualTo(expected);
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
            assertThat(declaracionService.hayInvalidas(List.of(solicitudValida()))).isFalse();
        }

        @Test
        @DisplayName("fecha con formato incorrecto es inválida")
        void fechaInvalida_devuelveTrue() {
            DeclaracionRequest req = solicitud(CUIT, "2024-03-15", "900", "100.00", "5.00", "5.00");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("jurisdicción no numérica es inválida")
        void jurisdiccionInvalida_devuelveTrue() {
            DeclaracionRequest req = solicitud(CUIT, FECHA, "abc", "100.00", "5.00", "5.00");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("monto no numérico es inválido")
        void montoInvalido_devuelveTrue() {
            DeclaracionRequest req = solicitud(CUIT, FECHA, "900", "cien", "5.00", "5.00");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("alícuota no numérica es inválida")
        void alicuotaInvalida_devuelveTrue() {
            DeclaracionRequest req = solicitud(CUIT, FECHA, "900", "100.00", "cinco", "5.00");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("montoPercibido no numérico es inválido")
        void montoPercibidoInvalido_devuelveTrue() {
            DeclaracionRequest req = solicitud(CUIT, FECHA, "900", "100.00", "5.00", "cinco");
            assertThat(declaracionService.hayInvalidas(List.of(req))).isTrue();
        }

        @Test
        @DisplayName("una sola solicitud inválida entre varias válidas alcanza para devolver true")
        void unaInvalidaEntreVarias_devuelveTrue() {
            DeclaracionRequest valida = solicitudValida();
            DeclaracionRequest invalida = solicitud(CUIT, "fecha-mala", "900", "100.00", "5.00", "5.00");

            assertThat(declaracionService.hayInvalidas(List.of(valida, invalida))).isTrue();
        }
    }
}