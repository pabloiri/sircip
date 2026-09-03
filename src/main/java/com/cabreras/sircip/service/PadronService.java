package com.cabreras.sircip.service;

import com.cabreras.sircip.entity.Padron;
import com.cabreras.sircip.entity.PadronId;
import com.cabreras.sircip.repo.PadronRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PadronService {

    private final PadronRepository padronRepository;

    public @NonNull Optional<Padron> getPadron(YearMonth periodo, String cuit) {
        int periodoParam = (periodo.getYear() * 100) + periodo.getMonthValue();
        PadronId idCompuesto = new PadronId(periodoParam, cuit);
        return padronRepository.findById(idCompuesto);
    }

}
