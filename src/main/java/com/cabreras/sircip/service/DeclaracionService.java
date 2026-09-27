package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.DeclaracionRequest;
import com.cabreras.sircip.dto.PercepcionTotal;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DeclaracionService {

    private static final String SEPARADOR = ",";
    public static final String TIPO = "1";
    public static final String TIPO_REGISTRO = "1";
    public static final String CODIGO_OPERACION = "0";
    public static final String ABM = "A";
    public static final String CRC_DEVOLUCIONES = "";
    public static final String DIFERENCIAS = "DIFERENCIAS";
    private static final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PadronService padronService;
    private final PercepcionService percepcionService;

    public @NonNull String declaracion(List<DeclaracionRequest> solicitudes) {
        List<String> lineas = new ArrayList<>();
        List<String> difTxt = new ArrayList<>();
        for (DeclaracionRequest req : solicitudes) {
            construirLineasTxt(req, lineas, difTxt);
        }
        String txt = String.join("\n", lineas);
        if (!difTxt.isEmpty()) {
            txt = txt + "\n" + DIFERENCIAS + "\n" + String.join("\n", difTxt);
        }
        return txt;
    }

    private void construirLineasTxt(DeclaracionRequest req, List<String> lineas, List<String> diferencias) {
        PercepcionTotal percepcionSircip = percepcionService.percepcionSircip(req);
        String crc = padronService.obtenerCrc(req.cuit(), req.fecha());
        lineas.add(construirLineaTxt(req, crc));
        if (hayDiferencias(req, percepcionSircip)) {
            DeclaracionRequest difReq = construirDiferencia(req, percepcionSircip);
            diferencias.add(construirLineaTxt(difReq, crc));
        }
    }

    private @NonNull String construirLineaTxt(DeclaracionRequest req, String crc) {
        return String.join(SEPARADOR,
                aString(req.cuit()), crc, aString(req.fecha()), TIPO, TIPO_REGISTRO,
                CODIGO_OPERACION, aString(req.jurisdiccion()), aString(req.tipoComprobante()),
                aString(req.letra()), aString(req.puntoVenta()), aString(req.numeroComprobante()),
                aString(req.monto()), aString(req.alicuota()), aString(req.montoPercibido()),
                aString(req.comprobanteOriginal()), CRC_DEVOLUCIONES, ABM);
    }

    private DeclaracionRequest construirDiferencia(DeclaracionRequest req, PercepcionTotal percepcionTotal) {
        return new DeclaracionRequest(req.cuit(), req.fecha(), req.jurisdiccion(),
        req.tipoComprobante(), req.letra(), req.puntoVenta(), req.numeroComprobante(), req.monto(),
        percepcionTotal.alicuota().toString(), percepcionTotal.percepcion().toString(), req.comprobanteOriginal());
    }

    private static boolean hayDiferencias(DeclaracionRequest req, PercepcionTotal percepcionTotal) {
        return percepcionTotal.alicuota().compareTo(new BigDecimal(req.alicuota())) != 0 ||
                percepcionTotal.percepcion().compareTo(new BigDecimal(req.montoPercibido())) != 0;
    }

    private String aString(Object objeto) {
        return objeto == null ? "" : objeto.toString();
    }

    public boolean hayInvalidas(List<DeclaracionRequest> solicitudes) {
        // TODO : cambiar esto para validar al procesar
        return solicitudes.stream().anyMatch(this::esInvalida);
    }

    private boolean esInvalida(DeclaracionRequest req) {
        try {
            LocalDate.parse(req.fecha(), formateadorFecha);
            Short.valueOf(req.jurisdiccion());
            new BigDecimal(req.monto());
            new BigDecimal(req.alicuota());
            new BigDecimal(req.montoPercibido());
            return false;
        } catch (DateTimeParseException | NumberFormatException e) {
            return true;
        }
    }

}
