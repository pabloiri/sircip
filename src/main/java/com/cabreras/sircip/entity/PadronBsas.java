package com.cabreras.sircip.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "padron_902")
@Getter
public class PadronBsas extends PadronAlicuota {
}

