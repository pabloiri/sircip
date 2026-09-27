package com.cabreras.sircip.controller;

import com.cabreras.sircip.dto.DeclaracionRequest;
import com.cabreras.sircip.entity.Padron;
import com.cabreras.sircip.dto.PercepcionResponse;
import com.cabreras.sircip.service.DeclaracionService;
import com.cabreras.sircip.service.PadronService;
import com.cabreras.sircip.service.PercepcionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping(path = "/taxengine/v1/")
@AllArgsConstructor
@Validated
public class PadronController {

    public static final String CUIT_MSG = "El CUIT debe tener exactamente 11 dígitos numéricos.";

    private final PadronService padronService;
    private final PercepcionService percepcionService;
    private final DeclaracionService declaracionService;

    @GetMapping(path = "/percepciones")
    public ResponseEntity<List<PercepcionResponse>> percepcion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @Pattern(regexp = "^\\d{11}$", message = CUIT_MSG) String cuit,
            @RequestParam @Min(901) @Max(924) Short jurisdiccion,
            @RequestParam(required = false) BigDecimal baseImponible) {
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

    // Temporal, por si hace falta.
    // Borrarlo si no se usa.
    @PostMapping(path = "/generacion-txt")
    public ResponseEntity<byte[]> generarZipConTxts(@RequestBody List<String> valores) {
        if (valores == null || valores.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String fechaActual = LocalDateTime.now().format(formateador);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // --- ARCHIVO 1: Todos los valores juntos ---
            String contenidoCompleto = String.join("\n", valores);
            byte[] datosArchivo1 = contenidoCompleto.getBytes(StandardCharsets.UTF_8);

            ZipEntry entrada1 = new ZipEntry("lista_completa_" + fechaActual + ".txt");
            zos.putNextEntry(entrada1);
            zos.write(datosArchivo1);
            zos.closeEntry();

            // --- ARCHIVO 2: Resumen o metadatos ---
            String contenidoResumen = "Total de elementos procesados: " + valores.size();
            byte[] datosArchivo2 = contenidoResumen.getBytes(StandardCharsets.UTF_8);

            ZipEntry entrada2 = new ZipEntry("resumen_" + fechaActual + ".txt");
            zos.putNextEntry(entrada2);
            zos.write(datosArchivo2);
            zos.closeEntry();

            zos.finish();
            byte[] archivoZipBytes = baos.toByteArray();

            String nombreZip = "archivos_generados_" + fechaActual + ".zip";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", nombreZip);
            headers.setContentLength(archivoZipBytes.length);

            return new ResponseEntity<>(archivoZipBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
