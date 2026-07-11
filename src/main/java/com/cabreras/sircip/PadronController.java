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
@RequestMapping(path ="/taxengine/v1")
@AllArgsConstructor
@Validated
public class PadronController {

    public static final BigDecimal ALICUOTA_FUERA_PADRON = BigDecimal.valueOf(2).setScale(2, HALF_UP);
    public static final BigDecimal ALICUOTA_SOBRETASA = BigDecimal.valueOf(1).setScale(2, HALF_UP);
    public static final BigDecimal CIEN = BigDecimal.valueOf(100);

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
        var respuestaSIRC = calcularRespuesta("SIRC", baseImponible, alicuota);
        respuesta.add(respuestaSIRC);
        if (haySobretasa(padron.getCampo7() + "", jurisdiccion)) {
            var respuestaSIRX = calcularRespuesta("SIRX", baseImponible, ALICUOTA_SOBRETASA);
            respuesta.add(respuestaSIRX);
        }
        return ResponseEntity.ok(respuesta);
    }

    private ResponseEntity<List<PadronResponse>> respuestaFueraPadron(Short jurisdiccion, BigDecimal baseImponible) {
        if (!jurisdiccionesCache.adheridaSircip(jurisdiccion))
            return ResponseEntity.notFound().build();
        var respuestaSIRY = calcularRespuesta("SIRY", baseImponible, ALICUOTA_FUERA_PADRON);
        return ResponseEntity.ok(List.of(respuestaSIRY));
    }

    private boolean haySobretasa(String campo7, Short jurisdiccion) {
        int indice = 924 - jurisdiccion;
        if (campo7 != null && indice >= 0 && indice < campo7.length()) {
            return campo7.charAt(indice) == '2';
        }
        return false;
    }

    private PadronResponse calcularRespuesta(String codigoImpuesto, BigDecimal baseImponible, BigDecimal alicuota) {
        if (baseImponible == null || baseImponible.equals(BigDecimal.ZERO))
            return new PadronResponse(codigoImpuesto, alicuota, null, null);
        BigDecimal impuesto = baseImponible
                .multiply(alicuota)
                .divide(CIEN, 2, HALF_UP);
        return new PadronResponse(codigoImpuesto, alicuota, baseImponible, impuesto);
    }

}
