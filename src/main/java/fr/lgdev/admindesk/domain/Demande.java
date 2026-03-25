package fr.lgdev.admindesk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "demandes")
@Getter
@Setter
@NoArgsConstructor
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String reference;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    @Column(nullable = false)
    private String nomDemandeur;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 150)
    @Column(nullable = false)
    private String emailDemandeur;

    @Size(max = 20)
    private String telephoneDemandeur;

    @NotNull(message = "Le type est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeDemande type;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 20, max = 4000, message = "La description doit contenir entre 20 et 4000 caractères")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priorite priorite = Priorite.NORMALE;

    @Size(max = 100)
    private String agentTraitant;

    @Column(columnDefinition = "TEXT")
    private String commentaireAgent;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_mise_a_jour")
    private LocalDateTime dateMiseAJour;

    public Demande(String reference, String nomDemandeur, String emailDemandeur,
                   String telephoneDemandeur, TypeDemande type, String description,
                   StatutDemande statut, Priorite priorite, String agentTraitant) {
        this.reference          = reference;
        this.nomDemandeur       = nomDemandeur;
        this.emailDemandeur     = emailDemandeur;
        this.telephoneDemandeur = telephoneDemandeur;
        this.type               = type;
        this.description        = description;
        this.statut             = statut;
        this.priorite           = priorite;
        this.agentTraitant      = agentTraitant;
    }
}
