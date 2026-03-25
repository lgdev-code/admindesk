package fr.lgdev.admindesk.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title       = "AdminDesk API",
                description = "API REST du projet fil rouge – Formation IA dans les Développements (IFAP 2026)",
                version     = "1.0"
        )
)
public class OpenApiConfig {}
