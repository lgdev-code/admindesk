package fr.lgdev.admindesk.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enregistre les outils AdminDesk auprès du serveur MCP (Spring AI).
 * <p>
 * Sans ce bean, les méthodes annotées {@code @Tool} de {@link DemandeMcpTools} ne sont
 * PAS exposées : l'auto-configuration du serveur MCP ne scanne que les beans
 * {@link ToolCallbackProvider}.
 */
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider adminDeskTools(DemandeMcpTools demandeMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(demandeMcpTools)
                .build();
    }
}
