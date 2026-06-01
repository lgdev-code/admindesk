package fr.lgdev.admindesk.service.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Masquage RGPD des données personnelles à structure fixe AVANT envoi au LLM.
 *
 * Couvre NIR, téléphone (NC 8 / FR 10 chiffres) et IBAN FR. Approche regex :
 * déterministe et auditable. Ne couvre PAS le texte libre (noms, adresses) —
 * décision projet documentée (cf. slide « Limites du masquage par regex »).
 *
 * sanitize() est idempotent : les placeholders ne contiennent aucun chiffre,
 * un second passage ne re-matche donc rien.
 */
@Component
public class InputSanitizer {

    // IBAN FR : FR + 2 chiffres de clé + 23 caractères alphanumériques (espaces tolérés)
    private static final Pattern IBAN =
            Pattern.compile("\\bFR\\d{2}(?:\\s?[A-Z0-9]){23}\\b");

    // NIR : 15 chiffres, espaces tolérés entre les groupes
    private static final Pattern NIR =
            Pattern.compile("\\b\\d(?:\\s?\\d){14}\\b");

    // Téléphone : 8 (NC) à 10 (FR) chiffres, espaces / points tolérés
    private static final Pattern TEL =
            Pattern.compile("\\b(?:\\d[\\s.]?){7,9}\\d\\b");

    /**
     * Remplace NIR / IBAN / téléphone par des placeholders typés.
     * Ordre important : IBAN et NIR (les plus spécifiques) avant le téléphone,
     * pour éviter que la regex téléphone ne happe une partie d'un IBAN ou d'un NIR.
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        String out = input;
        out = IBAN.matcher(out).replaceAll("[IBAN]");
        out = NIR.matcher(out).replaceAll("[NIR]");
        out = TEL.matcher(out).replaceAll("[TEL]");
        return out;
    }
}
