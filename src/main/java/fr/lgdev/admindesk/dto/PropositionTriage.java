package fr.lgdev.admindesk.dto;

import java.util.List;

/**
 * Proposition de triage produite par l'agent.
 *
 * <p>Convention AdminDesk : les DTO de sortie sont des records (immuables).
 * Ce record est aussi la cible du <em>structured output</em> : la reponse finale
 * de l'agent y est mappee via {@code .call().entity(PropositionTriage.class)}.</p>
 *
 * <p>IMPORTANT : ce n'est qu'une PROPOSITION. Aucune Demande n'est creee tant qu'un
 * agent humain ne l'a pas validee. L'agent decrit ; il n'agit pas.</p>
 *
 * @param typeDemande         nom d'un TypeDemande (ex. URBANISME, VOIRIE) choisi par l'agent
 * @param priorite            niveau propose : BASSE, NORMALE, HAUTE ou URGENTE
 * @param resume              resume court de la demande, en francais administratif
 * @param delaiIndicatifJours delai indicatif d'instruction (issu de l'outil reglesDuService)
 * @param piecesRequises      pieces justificatives a fournir (issu de reglesDuService)
 * @param justification       pourquoi ce routage, en une a deux phrases
 * @param sources             sources reglementaires citees (issu de rechercherReglementation)
 */
public record PropositionTriage(
        String typeDemande,
        String priorite,
        String resume,
        int delaiIndicatifJours,
        List<String> piecesRequises,
        String justification,
        List<String> sources
) {
}
