package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.dto.PercepcionStrategyRequest;
import com.cabreras.sircip.entity.PadronBsas;
import com.cabreras.sircip.entity.PadronId;
import com.cabreras.sircip.repo.PadronBsasRepository;
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
@Order(2)
@AllArgsConstructor
public class PercepcionBsAsStrategy implements PercepcionStrategy {

    public static final String BSAS = "BSAS";
    public static final short JURISDICCION_BSAS = (short) 902;
    public static final BigDecimal ALICUOTA_8 = new BigDecimal("8.00");

    private PadronBsasRepository padronRepository;

    @Override
    public List<PercepcionResponse> percepciones(PercepcionStrategyRequest request) {
        var padron = getPadron(request.periodo(), request.cuit());
        if (padron.isEmpty() && request.jurisdiccion() != JURISDICCION_BSAS &&
                !request.jurisdicciones().contains(JURISDICCION_BSAS))
            return List.of();
        var alicuota = padron.map(PadronBsas::getAlicuota).orElse(ALICUOTA_8);
        var importe = request.monto().multiply(alicuota).divide(BD_100, RoundingMode.HALF_UP);
        var response = new PercepcionResponse(BSAS, alicuota, request.monto(), importe);
        return List.of(response);
    }

    public @NonNull Optional<PadronBsas> getPadron(YearMonth periodo, String cuit) {
        int periodoParam = (periodo.getYear() * 100) + periodo.getMonthValue();
        PadronId idCompuesto = new PadronId(periodoParam, cuit);
        return padronRepository.findById(idCompuesto);
    }

}
