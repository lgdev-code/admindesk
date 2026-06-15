package fr.lgdev.admindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Saisie de l'agent au guichet : la demande du citoyen en texte libre.
 *
 * <p>Convention AdminDesk : les DTO d'entree utilisent {@code @Data} (mutables,
 * lies au formulaire Thymeleaf).</p>
 */
@Data
public class TriageDemandeForm {

    @NotBlank(message = "Decrivez la demande du citoyen")
    @Size(min = 10, max = 4000)
    private String texte;
}
