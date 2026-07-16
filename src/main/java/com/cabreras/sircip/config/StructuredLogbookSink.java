package com.cabreras.sircip.config;

import org.slf4j.MDC;
import org.zalando.logbook.*;
import org.springframework.stereotype.Component;
import org.zalando.logbook.core.DefaultHttpLogWriter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StructuredLogbookSink implements Sink {

    private final HttpLogWriter writer = new DefaultHttpLogWriter();

    @Override
    public void write(Precorrelation precorrelation, HttpRequest request) throws IOException {
        try {
            // Datos del request
            MDC.put("request.method", request.getMethod());
            MDC.put("request.uri", request.getRequestUri().toString());
            MDC.put("request.headers", request.getHeaders().toString());
            MDC.put("request.correlation", precorrelation.getId());

            // 👇 Extraer parámetros de la URL
            Map<String, String> parameters = extractParameters(request);
            String parametersJson = parameters.entrySet().stream()
                    .map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                    .collect(Collectors.joining(",", "{", "}"));
            MDC.put("request.parameters", parametersJson);

            // Body del request
            byte[] bodyBytes = request.getBody();
            String body = (bodyBytes != null) ? new String(bodyBytes, StandardCharsets.UTF_8) : "";
            MDC.put("request.body", body);

            writer.write(precorrelation, "Request logged");
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void write(Correlation correlation, HttpRequest request, HttpResponse response) throws IOException {
        try {
            MDC.put("response.status", String.valueOf(response.getStatus()));
            MDC.put("response.headers", response.getHeaders().toString());

            byte[] bodyBytes = response.getBody();
            String body = (bodyBytes != null) ? new String(bodyBytes, StandardCharsets.UTF_8) : "";
            MDC.put("response.body", body);

            MDC.put("response.duration", String.valueOf(correlation.getDuration()));

            writer.write(correlation, "Response logged");
        } finally {
            MDC.clear();
        }
    }

    /**
     * Extrae los parámetros de la URL como un mapa
     */
    private Map<String, String> extractParameters(HttpRequest request) {
        String uri = request.getRequestUri().toString();
        return UriComponentsBuilder.fromUriString(uri)
                .build()
                .getQueryParams()
                .toSingleValueMap();
    }
}