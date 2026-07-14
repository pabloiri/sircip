package com.cabreras.sircip;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

@RestController
@RequestMapping(path = "/taxengine/v1/")
@AllArgsConstructor
@Validated
public class PadronController {

    private static final int SCALE = 2;
    private static final int SCALE_MULTIPLIER = 100;
    private static final long ALICUOTA_FUERA_PADRON = 200L;    // 2.00%
    private static final long ALICUOTA_SOBRETASA = 100L;       // 1.00%
    private static final long CIEN_PORCIENTO = 10000L;         // 100.00% (en escala 2)
    private static final BigDecimal SCALE_MULTIPLIER_BD = BigDecimal.valueOf(SCALE_MULTIPLIER);

    private final PadronRepository padronRepository;
    private final AlicuotaCache alicuotaCache;
    private final JurisdiccionesCache jurisdiccionesCache;

    @GetMapping(path = "/percepciones")
    public ResponseEntity<List<PadronResponse>> consultarPadron(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String cuit,
            @RequestParam @Min(901) @Max(924) Short jurisdiccion,
            @RequestParam(required = false) BigDecimal baseImponible) {

        int periodoParam = (fecha.getYear() * 100) + fecha.getMonthValue();
        PadronId idCompuesto = new PadronId(periodoParam, cuit);
        return padronRepository.findById(idCompuesto)
                .map(padron -> respuestaEnPadron(jurisdiccion, baseImponible, padron))
                .orElseGet(() -> respuestaFueraPadron(jurisdiccion, baseImponible));
    }

    private ResponseEntity<List<PadronResponse>> respuestaEnPadron(Short jurisdiccion, BigDecimal baseImponible, Padron padron) {
        List<PadronResponse> respuesta = new ArrayList<>();
        var alicuota = alicuotaCache.obtenerPorcentaje(padron.getLetraAlicuota());
        var baseLong = bigDecimalToLong(baseImponible);
        var respuestaSIRC = calcularRespuesta("SIRC", baseLong, alicuota);
        respuesta.add(respuestaSIRC);
        if (haySobretasa(padron.getCampo7() + "", jurisdiccion)) {
            var respuestaSIRX = calcularRespuesta("SIRX", baseLong, ALICUOTA_SOBRETASA);
            respuesta.add(respuestaSIRX);
        }
        return ResponseEntity.ok(respuesta);
    }

    private ResponseEntity<List<PadronResponse>> respuestaFueraPadron(Short jurisdiccion, BigDecimal baseImponible) {
        if (!jurisdiccionesCache.adheridaSircip(jurisdiccion))
            return ResponseEntity.notFound().build();
        var baseLong = bigDecimalToLong(baseImponible);
        var respuestaSIRY = calcularRespuesta("SIRY", baseLong, ALICUOTA_FUERA_PADRON);
        return ResponseEntity.ok(List.of(respuestaSIRY));
    }

    private boolean haySobretasa(String campo7, Short jurisdiccion) {
        int indice = 924 - jurisdiccion;
        return campo7 != null && indice >= 0 && indice < campo7.length() && campo7.charAt(indice) == '2';
    }

    private PadronResponse calcularRespuesta(String codigoImpuesto, Long baseImponible, Long alicuota) {
        if (baseImponible == null || baseImponible == 0L)
            return new PadronResponse(codigoImpuesto, longToBigDecimal(alicuota), null, null);
        else
            return new PadronResponse(codigoImpuesto,
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
