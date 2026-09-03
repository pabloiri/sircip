package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.DeclaracionRequest;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DeclaracionService {

    private static final String SEPARADOR = ",";
    public static final String TIPO = "1";
    public static final String TIPO_REGISTRO = "1";
    public static final String CODIGO_OPERACION = "0";
    public static final String ABM = "A";
    public static final String CRC_DEVOLUCIONES = "";
    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PadronService padronService;

    public @NonNull String declaracion(List<DeclaracionRequest> solicitudes) {
        return solicitudes.stream()
                .map(this::construirRegistro)
                .collect(Collectors.joining("\n"));
    }

    private String construirRegistro(DeclaracionRequest req) {
        String crc = obtenerCrc(req.cuit(), req.fecha());
        return aString(req.cuit()) + SEPARADOR +
                crc + SEPARADOR +
                aString(req.fecha()) + SEPARADOR +
                TIPO + SEPARADOR +
                TIPO_REGISTRO + SEPARADOR +
                CODIGO_OPERACION + SEPARADOR +
                aString(req.jurisdiccion()) + SEPARADOR +
                aString(req.tipoComprobante()) + SEPARADOR +
                aString(req.letra()) + SEPARADOR +
                aString(req.puntoVenta()) + SEPARADOR +
                aString(req.numeroComprobante()) + SEPARADOR +
                aString(req.monto()) + SEPARADOR +
                aString(req.alicuota()) + SEPARADOR +
                aString(req.montoPercibido()) + SEPARADOR +
                aString(req.comprobanteOriginal()) + SEPARADOR +
                CRC_DEVOLUCIONES + SEPARADOR +
                ABM;    }

    private String obtenerCrc(String cuit, String fechaStr) {
        if (cuit == null || fechaStr == null)
            return ""; // valor si faltan datos
        try {
            LocalDate fecha = LocalDate.parse(fechaStr, formateadorFecha);
            YearMonth periodo = YearMonth.from(fecha);
            return padronService.getPadron(periodo, cuit)
                    .map(padron -> String.valueOf(padron.getCrc()))
                    .orElse("");
        } catch (DateTimeParseException e) {
            return "";
        }
    }

    private String aString(Object objeto) {
        return objeto == null ? "" : objeto.toString();
    }

}
