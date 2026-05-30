package fr.lgdev.admindesk.dto;

import fr.lgdev.admindesk.domain.Demande;
import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DemandeResponseDTO {

    private Long          id;
    private String        reference;
    private String        nomDemandeur;
    private String        emailDemandeur;
    private String        telephoneDemandeur;
    private TypeDemande   type;
    private String        typeLibelle;
    private String        description;
    private StatutDemande statut;
    private String        statutLibelle;
    private Priorite      priorite;
    private String        prioriteLibelle;
    private String        agentTraitant;
    private String        commentaireAgent;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;

    public static DemandeResponseDTO from(Demande d) {
        var dto = new DemandeResponseDTO();
        dto.setId(d.getId());
        dto.setReference(d.getReference());
        dto.setNomDemandeur(d.getNomDemandeur());
        dto.setEmailDemandeur(d.getEmailDemandeur());
        dto.setTelephoneDemandeur(d.getTelephoneDemandeur());
        dto.setType(d.getType());
        dto.setTypeLibelle(d.getType().getLibelle());
        dto.setDescription(d.getDescription());
        dto.setStatut(d.getStatut());
        dto.setStatutLibelle(d.getStatut().getLibelle());
        dto.setPriorite(d.getPriorite());
        dto.setPrioriteLibelle(d.getPriorite().getLibelle());
        dto.setAgentTraitant(d.getAgentTraitant());
        dto.setCommentaireAgent(d.getCommentaireAgent());
        dto.setDateCreation(d.getDateCreation());
        dto.setDateMiseAJour(d.getDateMiseAJour());
        return dto;
    }
}
