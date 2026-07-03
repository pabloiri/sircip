package com.cabreras.sircip;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class MyControllerTest {

    public static final String ENDPOINT =
            "/api/v1/padron?fecha=2026-06-28&cuit=2706170955&jurisdiccion=906&baseImponible=123";
    @Autowired
    private MockMvc mockMvc;

    @Test
    void cuandoPeticionNoTieneApiKey_entoncesDevuelve401() throws Exception {
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cuandoPeticionTieneApiKeyValida_entoncesDevuelve200() throws Exception {
        mockMvc.perform(get(ENDPOINT).header("X-API-KEY", "MiClaveSecreta123"))
                .andExpect(status().isNotFound());
    }
}