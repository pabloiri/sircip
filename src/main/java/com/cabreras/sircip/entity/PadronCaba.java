package com.cabreras.sircip.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "padron_901")
@Getter
public class PadronCaba extends PadronAlicuota {
}

