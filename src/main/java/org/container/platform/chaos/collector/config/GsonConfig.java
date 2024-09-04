package org.container.platform.chaos.collector.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GsonConfig 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 */
@Configuration
public class GsonConfig {

    @Bean
    public GsonBuilder gsonBuilder() {
        return new GsonBuilder();
    }

    /**
     * Gson Builder
     *
     * @param builder the another gson builder
     * @return the gson builder
     */
    @Bean
    @Autowired
    public Gson gson(GsonBuilder builder) {
        return builder.create();
    }
}