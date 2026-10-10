package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.DeclaracionRequest;
import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.entity.Padron;
import com.cabreras.sircip.repo.AlicuotaCache;
import com.cabreras.sircip.repo.JurisdiccionesCache;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class PercepcionService {

    private static final int SCALE = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal ALICUOTA_FUERA_PADRON = new BigDecimal("2.00");  // 2%
    private static final BigDecimal ALICUOTA_SOBRETASA = new BigDecimal("1.00");     // 1%

    public static final String SIRC = "SIRC";
    public static final String SIRX = "SIRX";
    public static final String SIRY = "SIRY";

    private static final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PadronService padronService;
    private final AlicuotaCache alicuotaCache;
    private final JurisdiccionesCache jurisdiccionesCache;

    public List<PercepcionResponse> percepcion(LocalDate fecha, String cuit, Short jurisdiccion, BigDecimal monto) {
        YearMonth periodo = YearMonth.from(fecha);
        return padronService.getPadron(periodo, cuit)
                .map(padron -> respuestaEnPadron(jurisdiccion, monto, padron))
                .orElseGet(() -> respuestaFueraPadron(jurisdiccion, monto, periodo));
    }

    public List<PercepcionResponse> percepciones(DeclaracionRequest req) {
        LocalDate fecha = LocalDate.parse(req.fecha(), formateadorFecha);
        Short jurisdiccion = Short.valueOf(req.jurisdiccion());
        BigDecimal monto = new BigDecimal(req.monto());
        return percepcion(fecha, req.cuit(), jurisdiccion, monto);
    }

    private List<PercepcionResponse> respuestaEnPadron(Short jurisdiccion, BigDecimal monto, Padron padron) {
        List<PercepcionResponse> respuesta = new ArrayList<>();
        BigDecimal alicuota = alicuotaCache.obtenerPorcentaje(padron.getLetraAlicuota());
        respuesta.add(calcularRespuesta(SIRC, monto, alicuota));
        if (haySobretasa(padron.getCampo7() + "", jurisdiccion)) {
            respuesta.add(calcularRespuesta(SIRX, monto, ALICUOTA_SOBRETASA));
        }
        return respuesta;
    }

    private List<PercepcionResponse> respuestaFueraPadron(Short jurisdiccion, BigDecimal monto, YearMonth periodo) {
        if (!jurisdiccionesCache.adheridaSircip(jurisdiccion, periodo))
            return Collections.emptyList();
        return List.of(calcularRespuesta(SIRY, monto, ALICUOTA_FUERA_PADRON));
    }

    private boolean haySobretasa(String campo7, Short jurisdiccion) {
        int indice = 924 - jurisdiccion;
        return campo7 != null && indice >= 0 && indice < campo7.length() && campo7.charAt(indice) == '2';
    }

    private PercepcionResponse calcularRespuesta(String codigoImpuesto, BigDecimal monto, BigDecimal alicuota) {
        if (monto == null || monto.signum() == 0)
            return new PercepcionResponse(codigoImpuesto, alicuota, null, null);
        // Único punto de redondeo: base (2 dec.) x alícuota (2 dec.) / 100 -> 2 decimales
        BigDecimal percepcion = monto
                .multiply(alicuota)
                .divide(CIEN, SCALE, REDONDEO);
        return new PercepcionResponse(codigoImpuesto, alicuota, monto, percepcion);
    }
}