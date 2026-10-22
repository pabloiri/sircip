package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.dto.PercepcionStrategyRequest;
import com.cabreras.sircip.entity.PadronCaba;
import com.cabreras.sircip.entity.PadronId;
import com.cabreras.sircip.repo.JurisdiccionesCache;
import com.cabreras.sircip.repo.PadronCabaRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Component
@Order(1)
@AllArgsConstructor
public class PercepcionCabaStrategy implements PercepcionStrategy {

    public static final String CABA = "CABA";
    public static final short JURISDICCION_CABA = (short) 901;
    public static final BigDecimal ALICUOTA_1 = new BigDecimal("3.50");    // inscripto y no esta en padron
    public static final BigDecimal ALICUOTA_2 = new BigDecimal("6.00");    // no inscripto y no esta en padron

    private PadronCabaRepository padronRepository;
    private JurisdiccionesCache jurisdiccionesCache;

    @Override
    public List<PercepcionResponse> percepciones(PercepcionStrategyRequest request) {
        if (request.jurisdiccion() != JURISDICCION_CABA && !request.jurisdicciones().contains(JURISDICCION_CABA))
            return List.of();
        var padron = getPadron(request.periodo(), request.cuit());
        var alicuota = padron.map(PadronCaba::getAlicuota)
                .orElseGet(() -> request.jurisdicciones().contains(JURISDICCION_CABA) ? ALICUOTA_1 : ALICUOTA_2);
        var importe = request.monto().multiply(alicuota).divide(BD_100, RoundingMode.HALF_UP);
        var response = new PercepcionResponse(CABA, alicuota, request.monto(), importe);
        return List.of(response);
    }

    @Override
    public boolean adheridaSircip(YearMonth periodo) {
        return jurisdiccionesCache.adheridaSircip(JURISDICCION_CABA, periodo);
    }

    public @NonNull Optional<PadronCaba> getPadron(YearMonth periodo, String cuit) {
        int periodoParam = (periodo.getYear() * 100) + periodo.getMonthValue();
        PadronId idCompuesto = new PadronId(periodoParam, cuit);
        return padronRepository.findById(idCompuesto);
    }

}
