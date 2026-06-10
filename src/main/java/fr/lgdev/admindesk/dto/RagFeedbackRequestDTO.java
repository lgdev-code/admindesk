package fr.lgdev.admindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Vote de feedback sur une réponse RAG (👍/👎), posté par le frontend avec le requestId. */
@Data
@NoArgsConstructor
public class RagFeedbackRequestDTO {

    @NotBlank
    private String requestId;

    @NotBlank
    @Pattern(regexp = "UP|DOWN", message = "vote doit valoir UP ou DOWN")
    private String vote;
}
