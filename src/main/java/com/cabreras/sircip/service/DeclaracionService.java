package com.cabreras.sircip.service;

import com.cabreras.sircip.dto.DeclaracionRequest;
import com.cabreras.sircip.dto.PercepcionResponse;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public static final String DIFERENCIAS = "DIFERENCIAS";
    private static final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String ERROR_CALCULO =
        "No se pudo calcular la percepción para tipo comprobante %s, letra %s, número comprobante %s, punto venta %s";

    private final PadronService padronService;
    private final PercepcionService percepcionService;

    public @NonNull String declaracion(List<DeclaracionRequest> requests) {
        Map<ClaveComprobante, List<DeclaracionRequest>> requestsPorComprobante = requests.stream()
                .collect(Collectors.groupingBy(ClaveComprobante::clave, LinkedHashMap::new, Collectors.toList()));

        List<String> lineas = new ArrayList<>();
        List<String> diferencias = new ArrayList<>();
        requestsPorComprobante.values().forEach(requestsDeUnComprobante ->
                agregarLineasYDiferencias(requestsDeUnComprobante, lineas, diferencias));

        String txt = String.join("\n", lineas);
        if (!diferencias.isEmpty()) {
            txt = txt + "\n" + DIFERENCIAS + "\n" + String.join("\n", diferencias);
        }
        return txt;
    }

    private record ClaveComprobante(String tipoComprobante, String letra,
                                    String puntoVenta, String numeroComprobante) {
        static ClaveComprobante clave(DeclaracionRequest req) {
            return new ClaveComprobante(req.tipoComprobante(), req.letra(),
                    req.puntoVenta(), req.numeroComprobante());
        }
    }

    private void agregarLineasYDiferencias(List<DeclaracionRequest> requestsDeUnComprobante,
                                           List<String> lineas, List<String> diferencias) {
        DeclaracionRequest primero = requestsDeUnComprobante.getFirst();
        List<PercepcionResponse> calculadas = percepcionService.percepciones(primero);
        String crc = obtenerCrc(primero.cuit(), primero.fecha());

        requestsDeUnComprobante.forEach(request -> lineas.add(construirLineaTxt(request, crc)));

        if (hayDiferencias(requestsDeUnComprobante, calculadas)) {
            calculadas.forEach(calc ->
                    diferencias.add(construirLineaTxt(construirDiferencia(primero, calc), crc)));
            if (calculadas.isEmpty()) {
                diferencias.add(ERROR_CALCULO.formatted(primero.tipoComprobante(), primero.letra(),
                        primero.numeroComprobante(), primero.puntoVenta()));
            }
        }
    }

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

    private @NonNull String construirLineaTxt(DeclaracionRequest req, String crc) {
        return String.join(SEPARADOR,
                aString(req.cuit()), crc, aString(req.fecha()), TIPO, TIPO_REGISTRO,
                CODIGO_OPERACION, aString(req.jurisdiccion()), aString(req.tipoComprobante()),
                aString(req.letra()), aString(req.puntoVenta()), aString(req.numeroComprobante()),
                aString(req.monto()), aString(req.alicuota()), aString(req.montoPercibido()),
                aString(req.comprobanteOriginal()), CRC_DEVOLUCIONES, ABM);
    }

    private DeclaracionRequest construirDiferencia(DeclaracionRequest base, PercepcionResponse calculada) {
        return new DeclaracionRequest(base.cuit(), base.fecha(), base.jurisdiccion(),
                base.tipoComprobante(), base.letra(), base.puntoVenta(), base.numeroComprobante(),
                base.monto(), calculada.alicuota().toString(), calculada.importe().toString(),
                base.comprobanteOriginal());
    }

    // Solo se contempla el caso en que hay 1 o 2 elementos en cada lista
    // O sea 1 o 2 percepciones por comprobante
    private static boolean hayDiferencias(List<DeclaracionRequest> recibidas, List<PercepcionResponse> calculadas) {
        int n = recibidas.size();
        if (n != calculadas.size())
            return true;
        if (n == 1)
            return !coincide(recibidas.getFirst(), calculadas.getFirst());
        if (n == 2) {
            DeclaracionRequest r0 = recibidas.get(0), r1 = recibidas.get(1);
            PercepcionResponse c0 = calculadas.get(0), c1 = calculadas.get(1);
            boolean directo = coincide(r0, c0) && coincide(r1, c1);
            boolean cruzado = coincide(r0, c1) && coincide(r1, c0);
            return !(directo || cruzado);
        }
        return true;         // n > 2
    }

    private static boolean coincide(DeclaracionRequest req, PercepcionResponse calc) {
        return sonIguales(calc.alicuota(), new BigDecimal(req.alicuota()))
                && sonIguales(calc.importe(), new BigDecimal(req.montoPercibido()));
    }

    private static boolean sonIguales(BigDecimal bd1, BigDecimal bd2) {
        return bd1.setScale(2, RoundingMode.HALF_UP)
                .compareTo(bd2.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    private String aString(Object objeto) {
        return objeto == null ? "" : objeto.toString();
    }

    public boolean hayInvalidas(List<DeclaracionRequest> solicitudes) {
        return solicitudes.stream().anyMatch(this::esInvalida);
    }

    private boolean esInvalida(DeclaracionRequest req) {
        if (esVacio(req.fecha()) || esVacio(req.jurisdiccion())
                || esVacio(req.monto()) || esVacio(req.alicuota())
                || esVacio(req.montoPercibido())) {
            return true;
        }
        try {
            LocalDate.parse(req.fecha(), formateadorFecha);
            Short.valueOf(req.jurisdiccion());
            BigDecimal monto = new BigDecimal(req.monto());
            BigDecimal alicuota = new BigDecimal(req.alicuota());
            BigDecimal montoPercibido = new BigDecimal(req.montoPercibido());
            return malDecimales(monto) || malDecimales(alicuota) || malDecimales(montoPercibido);
        } catch (DateTimeParseException | NumberFormatException e) {
            return true;
        }
    }

    private boolean malDecimales(BigDecimal valor) {
        return valor.stripTrailingZeros().scale() > 2;
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

}
