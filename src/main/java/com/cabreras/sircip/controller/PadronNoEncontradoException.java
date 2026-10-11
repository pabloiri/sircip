package com.cabreras.sircip.controller;

import java.time.YearMonth;

public class PadronNoEncontradoException extends RuntimeException {

    public PadronNoEncontradoException(YearMonth periodo, String cuit) {
        super("No existe registro en el padrón para el CUIT " + cuit + " en el periodo " + periodo);
    }
}

