package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.entity.Padron;
import com.cabreras.sircip.repo.AlicuotaCache;
import com.cabreras.sircip.repo.JurisdiccionesCache;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

@Service
@AllArgsConstructor
public class PercepcionService {

    private static final int SCALE = 2;
    private static final int SCALE_MULTIPLIER = 100;
    private static final long ALICUOTA_FUERA_PADRON = 200L;    // 2.00%
    private static final long ALICUOTA_SOBRETASA = 100L;       // 1.00%
    private static final long CIEN_PORCIENTO = 10000L;         // 100.00% (en escala 2)
    private static final BigDecimal SCALE_MULTIPLIER_BD = BigDecimal.valueOf(SCALE_MULTIPLIER);

    private final PadronService padronService;
    private final AlicuotaCache alicuotaCache;
    private final JurisdiccionesCache jurisdiccionesCache;

    public List<PercepcionResponse> percepcion(LocalDate fecha, String cuit, Short jurisdiccion, BigDecimal baseImponible) {
        YearMonth periodo = YearMonth.from(fecha);
        return padronService.getPadron(periodo, cuit)
                .map(padron -> respuestaEnPadron(jurisdiccion, baseImponible, padron))
                .orElseGet(() -> respuestaFueraPadron(jurisdiccion, baseImponible));
    }

    private List<PercepcionResponse> respuestaEnPadron(Short jurisdiccion, BigDecimal baseImponible, Padron padron) {
        List<PercepcionResponse> respuesta = new ArrayList<>();
        var alicuota = alicuotaCache.obtenerPorcentaje(padron.getLetraAlicuota());
        var baseLong = bigDecimalToLong(baseImponible);
        var respuestaSIRC = calcularRespuesta("SIRC", baseLong, alicuota);
        respuesta.add(respuestaSIRC);
        if (haySobretasa(padron.getCampo7() + "", jurisdiccion)) {
            var respuestaSIRX = calcularRespuesta("SIRX", baseLong, ALICUOTA_SOBRETASA);
            respuesta.add(respuestaSIRX);
        }
        return respuesta;
    }

    private List<PercepcionResponse> respuestaFueraPadron(Short jurisdiccion, BigDecimal baseImponible) {
        if (!jurisdiccionesCache.adheridaSircip(jurisdiccion))
            return Collections.emptyList();
        var baseLong = bigDecimalToLong(baseImponible);
        var respuestaSIRY = calcularRespuesta("SIRY", baseLong, ALICUOTA_FUERA_PADRON);
        return List.of(respuestaSIRY);
    }

    private boolean haySobretasa(String campo7, Short jurisdiccion) {
        int indice = 924 - jurisdiccion;
        return campo7 != null && indice >= 0 && indice < campo7.length() && campo7.charAt(indice) == '2';
    }

    private PercepcionResponse calcularRespuesta(String codigoImpuesto, Long baseImponible, Long alicuota) {
        if (baseImponible == null || baseImponible == 0L)
            return new PercepcionResponse(codigoImpuesto, longToBigDecimal(alicuota), null, null);
        else
            return new PercepcionResponse(codigoImpuesto,
                    longToBigDecimal(alicuota),
                    longToBigDecimal(baseImponible),
                    longToBigDecimal((baseImponible * alicuota) / CIEN_PORCIENTO));
    }

    // Convierte BigDecimal a Long con escala 2 (multiplica por 100)
    private Long bigDecimalToLong(BigDecimal valor) {
        return valor == null ? null : valor.setScale(SCALE, HALF_UP).multiply(SCALE_MULTIPLIER_BD).longValue();
    }

    // Convierte Long (escala 2) a BigDecimal con 2 decimales
    private BigDecimal longToBigDecimal(Long valor) {
        return valor == null ? null : BigDecimal.valueOf(valor, SCALE);
    }

}
