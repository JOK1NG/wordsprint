package com.example.wordsprint.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class OpenApiConfig {

    @Bean
    public OpenAPI wordSprintOpenApi() {
        return new OpenAPI().info(new Info()
                .title("WordSprint API")
                .description("WordSprint 单词卡记忆训练平台接口文档")
                .version("0.0.1"));
    }
}
