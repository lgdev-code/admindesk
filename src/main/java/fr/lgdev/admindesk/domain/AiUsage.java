package fr.lgdev.admindesk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Enregistrement d'un usage du service IA, pour le décompte des quotas.
 * Une ligne par appel LLM réussi.
 */
@Entity
@Table(name = "ai_usage")
@Getter
@Setter
@NoArgsConstructor
public class AiUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long agentId;

    @Column(nullable = false)
    private Instant dateUsage;

    @Column(nullable = false)
    private int tokens;

    public AiUsage(Long agentId, Instant dateUsage, int tokens) {
        this.agentId = agentId;
        this.dateUsage = dateUsage;
        this.tokens = tokens;
    }
}
