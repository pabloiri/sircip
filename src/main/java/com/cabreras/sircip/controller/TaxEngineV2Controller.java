package com.cabreras.sircip.controller;

import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.service.PercepcionV2Service;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
import java.util.List;

@RestController
@RequestMapping(path = "/taxengine/v2/")
@AllArgsConstructor
@Validated
public class TaxEngineV2Controller {

    public static final String CUIT_MSG = "El CUIT debe tener exactamente 11 dígitos numéricos.";
    public static final String DECIMALES_MSG = "La base imponible admite como máximo 2 decimales";

    private final PercepcionV2Service percepcionService;

    @GetMapping(path = "/percepciones")
    public ResponseEntity<List<PercepcionResponse>> percepcion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @Pattern(regexp = "^\\d{11}$", message = CUIT_MSG) String cuit,
            @RequestParam @Min(901) @Max(924) Short jurisdiccion,
            @RequestParam List<Short> jurisdicciones,
            @RequestParam(required = false)
            @Digits(integer = 13, fraction = 2, message = DECIMALES_MSG)
            BigDecimal baseImponible) {
        List<PercepcionResponse> responses = percepcionService.percepciones(fecha, cuit, jurisdiccion, jurisdicciones, baseImponible);
        return ResponseEntity.ok(responses);
    }

}
