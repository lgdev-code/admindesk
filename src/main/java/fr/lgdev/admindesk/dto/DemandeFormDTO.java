package fr.lgdev.admindesk.dto;

import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DemandeFormDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nomDemandeur;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 150)
    private String emailDemandeur;

    @Size(max = 20)
    private String telephoneDemandeur;

    @NotNull(message = "Le type est obligatoire")
    private TypeDemande type;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 20, max = 4000)
    private String description;

    private StatutDemande statut = StatutDemande.EN_ATTENTE;
    private Priorite priorite = Priorite.NORMALE;

    @Size(max = 100)
    private String agentTraitant;

    @Size(max = 2000)
    private String commentaireAgent;
}
