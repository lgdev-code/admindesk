package fr.lgdev.admindesk.service.ai;

import org.springframework.stereotype.Component;

/**
 * TP9 — Limite de saisie appliquée AVANT l'envoi au LLM.
 *
 * Une description démesurée = appel coûteux/lent et grande surface pour cacher une injection.
 * On borne la taille et on REJETTE au-delà (400) plutôt que de tronquer, pour ne pas amputer
 * le sens de la demande. Conserve le null.
 *
 * NB : on ne tente PAS de « nettoyer » l'entrée par regex anti-injection — le modèle résiste
 * déjà aux injections par mot-clé, et un filtre regex donne une fausse impression de sécurité.
 * La vraie défense est la clause système (AIService) + la validation de sortie.
 */
@Component
public class PromptGuard {

    private static final int MAX_CHARS = 4000;

    public String check(String input) {
        if (input == null) {
            return null;
        }
        if (input.length() > MAX_CHARS) {
            throw new InputTooLargeException(
                    "Demande trop longue : " + input.length() + " caractères (max " + MAX_CHARS + ").");
        }
        return input;
    }
}
