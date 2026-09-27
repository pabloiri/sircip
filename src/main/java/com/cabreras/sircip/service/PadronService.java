package com.cabreras.sircip.service;

import com.cabreras.sircip.entity.Padron;
import com.cabreras.sircip.entity.PadronId;
import com.cabreras.sircip.repo.PadronRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PadronService {

    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PadronRepository padronRepository;

    public @NonNull Optional<Padron> getPadron(YearMonth periodo, String cuit) {
        int periodoParam = (periodo.getYear() * 100) + periodo.getMonthValue();
        PadronId idCompuesto = new PadronId(periodoParam, cuit);
        return padronRepository.findById(idCompuesto);
    }

    public String obtenerCrc(String cuit, String fechaStr) {
        if (cuit == null || fechaStr == null)
            return ""; // valor si faltan datos
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, formateadorFecha);
            YearMonth periodo = YearMonth.from(fecha);
            return getPadron(periodo, cuit)
                    .map(padron -> String.valueOf(padron.getCrc()))
                    .orElse("");
        } catch (DateTimeParseException e) {
            return "";
        }
    }

}
