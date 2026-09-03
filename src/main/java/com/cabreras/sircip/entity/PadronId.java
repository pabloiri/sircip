package com.cabreras.sircip.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
public class PadronId implements Serializable {
    private Integer periodo;
    private String cuit;
    public PadronId() {}

    public PadronId(Integer periodo, String cuit) {
        this.periodo = periodo;
        this.cuit = cuit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PadronId padronId = (PadronId) o;
        return Objects.equals(periodo, padronId.periodo) &&
                Objects.equals(cuit, padronId.cuit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodo, cuit);
    }
}
