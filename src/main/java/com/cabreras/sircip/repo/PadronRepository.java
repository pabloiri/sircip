package com.cabreras.sircip.repo;

import com.cabreras.sircip.entity.Padron;
import com.cabreras.sircip.entity.PadronId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PadronRepository extends JpaRepository<Padron, PadronId> {

}

