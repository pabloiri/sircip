package com.cabreras.sircip.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.Conditions;
import org.zalando.logbook.servlet.LogbookFilter;

@Configuration
public class LogbookConfig {

    @Bean
    public Logbook logbook(StructuredLogbookSink sink) {
        return Logbook.builder()
                .condition(Conditions.exclude(Conditions.requestTo("/actuator/**")))
                .sink(sink)
                .build();
    }

    @Bean
    public FilterRegistrationBean<LogbookFilter> logbookFilter(Logbook logbook) {
        FilterRegistrationBean<LogbookFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LogbookFilter(logbook));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}