package com.cabreras.sircip.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class PadronBase {

    @EmbeddedId
    private PadronId id;

}