package com.exciting.vvue.common.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.Arrays;


@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi group(){
        return GroupedOpenApi.builder()
                .group("vvue apis")
                .packagesToScan("com.exciting.vvue")
                .build();
    }
    @Bean
    public GroupedOpenApi developGroup() {
        return GroupedOpenApi.builder()
                .group("develop apis")
                .packagesToScan("com.exciting.vvue.develop") // package 필터 설정
                .build();
    }
    @Bean
    public OpenAPI springOpenApi(){
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI().components(new Components().addSecuritySchemes("bearerAuth",securityScheme)).security(Arrays.asList(securityRequirement))
                .info(new Info()
                .title("vvue REST API")
                .version("1.0.0")
                .description("vvue swagger api 입니다."));
    }


}