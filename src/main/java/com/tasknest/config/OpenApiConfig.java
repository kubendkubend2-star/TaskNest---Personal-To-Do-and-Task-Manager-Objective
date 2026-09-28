package com.tasknest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI taskNestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskNest – Personal To-Do and Task Manager REST API")
                        .description("RESTful backend API for TaskNest, enabling users to organize assignments, " +
                                "club work, and personal errands. Supports list organization, priorities, due dates, " +
                                "status toggling, cross-list moves, overdue tracking, filtering, and dashboard analytics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TaskNest Engineering Team")
                                .email("support@tasknest.local"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")
                ));
    }
}
