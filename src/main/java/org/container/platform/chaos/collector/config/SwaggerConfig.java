package org.container.platform.chaos.collector.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Swagger Config 클래스
 *
 * @author Luna
 * @version 1.0
 * @since 2024-08-30
 **/
@Configuration
public class SwaggerConfig {

    private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = new HashSet<>(
            Arrays.asList("application/json"));

    @Bean
    public Docket api() {

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v1.0")
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.container.platform.chaos.api"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .produces(DEFAULT_PRODUCES_AND_CONSUMES)
                .consumes(DEFAULT_PRODUCES_AND_CONSUMES);

    }

    @Bean
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("K-PaaS Container Platform API Docs")
                .version("v1.0")
                .description("This is a API Document created with swagger.")
                .license("Apache2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                .build();
    }

}