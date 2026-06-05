package fr.lgdev.admindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TP10 (D4) — Requête RAG : une question libre posée par un agent.
 */
@Data
@NoArgsConstructor
public class RagRequestDTO {

    @NotBlank(message = "La question est obligatoire")
    @Size(max = 1000, message = "La question ne doit pas dépasser 1000 caractères")
    private String question;
}
