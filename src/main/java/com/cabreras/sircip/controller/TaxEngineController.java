package com.cabreras.sircip.controller;

import com.cabreras.sircip.dto.DeclaracionRequest;
import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.entity.Padron;
import com.cabreras.sircip.service.DeclaracionService;
import com.cabreras.sircip.service.PadronService;
import com.cabreras.sircip.service.PercepcionService;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping(path = "/taxengine/v1/")
@AllArgsConstructor
@Validated
public class TaxEngineController {

    public static final String CUIT_MSG = "El CUIT debe tener exactamente 11 dígitos numéricos.";
    public static final String DECIMALES_MSG = "La base imponible admite como máximo 2 decimales";

    private final PadronService padronService;
    private final PercepcionService percepcionService;
    private final DeclaracionService declaracionService;

    @GetMapping(path = "/percepciones")
    public ResponseEntity<List<PercepcionResponse>> percepcion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @Pattern(regexp = "^\\d{11}$", message = CUIT_MSG) String cuit,
            @RequestParam @Min(901) @Max(924) Short jurisdiccion,
            @RequestParam(required = false)
            @Digits(integer = 13, fraction = 2, message = DECIMALES_MSG)
            BigDecimal baseImponible) {
        List<PercepcionResponse> responses = percepcionService.percepcion(fecha, cuit, jurisdiccion, baseImponible);
        return responses.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/padron")
    public ResponseEntity<Padron> padron(@RequestParam YearMonth periodo, @RequestParam String cuit) {
        return padronService.getPadron(periodo, cuit)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/declaracion")
    public ResponseEntity<byte[]> declaracion(@RequestBody List<DeclaracionRequest> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty() || declaracionService.hayInvalidas(solicitudes))
            return ResponseEntity.badRequest().build();
        byte[] datosArchivo = declaracionService.declaracion(solicitudes).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=declaracion.txt")
                .contentLength(datosArchivo.length)
                .body(datosArchivo);
    }

}
