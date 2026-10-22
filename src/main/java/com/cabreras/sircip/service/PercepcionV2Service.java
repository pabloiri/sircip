package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.dto.PercepcionStrategyRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class PercepcionV2Service {

    private PercepcionService percepcionV1Service;
    private final List<PercepcionStrategy> strategyList;

    public List<PercepcionResponse> percepciones(LocalDate fecha, String cuit, Short jurisdiccion,
                                                 List<Short> jurisdicciones, BigDecimal monto) {
        // TODO : ver si la jurisdiccion en el periodo esta adherida a SIRCIP para llamar o no a cada estrategia.
        YearMonth periodo = YearMonth.from(fecha);
        var request = new PercepcionStrategyRequest(periodo, cuit, jurisdiccion, jurisdicciones, monto);
        var percepcionesSircip = percepcionV1Service.percepcion(fecha, cuit, jurisdiccion, monto);
        var percepcionesNoSircip = strategyList.stream()
                .flatMap(strategy -> strategy.percepciones(request).stream())
                .toList();
        return Stream.concat(percepcionesSircip.stream(), percepcionesNoSircip.stream()).toList();
    }

}