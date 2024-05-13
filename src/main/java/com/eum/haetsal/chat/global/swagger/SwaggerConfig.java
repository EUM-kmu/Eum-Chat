package com.eum.haetsal.chat.global.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(@Value("${openapi.service.url}")String url) {
        return new OpenAPI()
                .servers(List.of(new Server().url(url)))
                .info(new Info()
                        .title("EUM Chat API")
                        .description("정릉 이음 어플의 채팅 기능을 제공합니다.")
                        .version("1.0.0"));
    }
}
