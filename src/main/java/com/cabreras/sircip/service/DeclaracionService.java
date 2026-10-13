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
import java.util.*;
import java.util.stream.Collectors;

import static com.cabreras.sircip.service.PercepcionService.*;

@Service
@AllArgsConstructor
public class DeclaracionService {

    private static final String SEPARADOR = ",";
    public static final String TIPO = "1";
    public static final String CODIGO_OPERACION = "0";
    public static final String ABM = "A";
    public static final String CRC_DEVOLUCIONES = "";
    public static final String DIFERENCIAS = "DIFERENCIAS";
    private static final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String TIPO_REGISTRO_SIRC = "1";
    private static final String TIPO_REGISTRO_SIRC_ALICUOTA_CERO = "2";
    private static final String TIPO_REGISTRO_SIRY = "4";
    private static final String TIPO_REGISTRO_SIRX = "5";
    private static final String ERROR_CALCULO =
            "No se pudo calcular la percepción para tipo comprobante %s, letra %s, número comprobante %s, punto venta %s";

    private final PadronService padronService;
    private final PercepcionService percepcionService;

    public boolean hayInvalidas(List<DeclaracionRequest> solicitudes) {
        return solicitudes.stream().anyMatch(this::esInvalida);
    }

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
        boolean hayDiferencias = false;

        List<PercepcionResponse> pendientes = new ArrayList<>(calculadas);
        for (DeclaracionRequest request : requestsDeUnComprobante) {
            Optional<PercepcionResponse> calculada = tomarCalculada(pendientes, request.alicuota());
            // sin calculada con esa alícuota, o con distinto importe: diferencia
            hayDiferencias |= calculada
                    .map(c -> !sonIguales(c.importe(), new BigDecimal(request.montoPercibido())))
                    .orElse(true);
            String tipo = calculada.map(DeclaracionService::tipoRegistro).orElse("");
            lineas.add(construirLineaTxt(request, crc, tipo));
        }
        hayDiferencias |= !pendientes.isEmpty();   // calculadas que no tenían línea recibida

        if (hayDiferencias) {
            calculadas.forEach(calc ->
                    diferencias.add(construirLineaTxt(construirDiferencia(primero, calc), crc, tipoRegistro(calc))));
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

    // Toma (y quita de la lista) la primera calculada con esa alícuota
    private static Optional<PercepcionResponse> tomarCalculada(List<PercepcionResponse> pendientes, String alicuota) {
        BigDecimal buscada = new BigDecimal(alicuota);
        Optional<PercepcionResponse> calculada = pendientes.stream()
                .filter(c -> sonIguales(c.alicuota(), buscada))
                .findFirst();
        calculada.ifPresent(pendientes::remove);
        return calculada;
    }

    private @NonNull String construirLineaTxt(DeclaracionRequest req, String crc, String tipoRegistro) {
        return String.join(SEPARADOR,
                aString(req.cuit()), crc, aString(req.fecha()), TIPO, tipoRegistro,
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

    private static boolean sonIguales(BigDecimal bd1, BigDecimal bd2) {
        return bd1.setScale(2, RoundingMode.HALF_UP)
                .compareTo(bd2.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    private String aString(Object objeto) {
        return objeto == null ? "" : objeto.toString();
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

    private static String tipoRegistro(PercepcionResponse calc) {
        return switch (calc.codigoImpuesto()) {
            case SIRC -> calc.alicuota().compareTo(BigDecimal.ZERO) == 0
                    ? TIPO_REGISTRO_SIRC_ALICUOTA_CERO
                    : TIPO_REGISTRO_SIRC;
            case SIRY -> TIPO_REGISTRO_SIRY;
            case SIRX -> TIPO_REGISTRO_SIRX;
            default -> "";
        };
    }

}
