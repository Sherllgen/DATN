package com.project.evgo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

/**
 * OpenAPI (Swagger) configuration.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EVGo API")
                .description("API for Electric Vehicle Charging Station Management System")
                .version("1.0.0")
                .contact(new Contact()
                    .name("EVGo Team")
                    .email("support@evgo.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Development Server")))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token. Get token from /api/v1/auth/login")))
            .tags(List.of(
                new Tag().name("Authentication").description("Authentication APIs"),
                new Tag().name("Users").description("User management APIs"),
                new Tag().name("User Vehicle Management").description("APIs for managing user's vehicles"),
                new Tag().name("Vehicle Catalog").description("APIs for electric motorcycle reference data"),
                new Tag().name("Account Administrator").description("APIs for administrators to manage accounts"),
                
                new Tag().name("Stations").description("Station management APIs"),
                new Tag().name("Station Photos").description("Station photo management APIs"),
                new Tag().name("Station Pricing").description("Station pricing management APIs"),
                new Tag().name("Station Administrator").description("APIs for administrators to manage stations"),
                new Tag().name("Chargers").description("Charger & Port management APIs"),
                
                new Tag().name("Bookings").description("Booking management APIs"),
                new Tag().name("Booking Metadata").description("Metadata for booking EV charging slots"),
                new Tag().name("Charging").description("Charging session management APIs"),
                
                new Tag().name("Invoices").description("Invoice lookup endpoints"),
                new Tag().name("ZaloPay").description("ZaloPay App-to-App payment APIs"),
                
                new Tag().name("Reviews").description("Station review APIs"),
                new Tag().name("Notifications").description("Notification management APIs"),
                new Tag().name("Push Notifications").description("Endpoints for managing push tokens"),
                new Tag().name("Navigation").description("Navigation API for routing and directions"),
                new Tag().name("Complaints").description("Complaint management APIs")
            ));
    }

    @Bean
    public OpenApiCustomizer sortTagsCustomizer() {
        return openApi -> {
            List<String> tagOrder = List.of(
                "Authentication",
                "Users",
                "User Vehicle Management",
                "Vehicle Catalog",
                "Account Administrator",
                
                "Stations",
                "Station Photos",
                "Station Pricing",
                "Station Administrator",
                "Chargers",
                
                "Bookings",
                "Booking Metadata",
                "Charging",
                
                "Invoices",
                "ZaloPay",
                
                "Reviews",
                "Notifications",
                "Push Notifications",
                "Navigation",
                "Complaints"
            );

            if (openApi.getTags() != null) {
                openApi.getTags().sort(Comparator.comparingInt(tag -> {
                    int index = tagOrder.indexOf(tag.getName());
                    return index == -1 ? Integer.MAX_VALUE : index;
                }));
            }
        };
    }
}
