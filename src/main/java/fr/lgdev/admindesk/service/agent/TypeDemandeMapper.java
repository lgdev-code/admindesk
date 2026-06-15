package fr.lgdev.admindesk.service.agent;

import fr.lgdev.admindesk.domain.TypeDemande;

/**
 * Resout en {@link TypeDemande} ce que l'agent renvoie sous forme de texte
 * (nom d'enum, libelle, ou approchant). Tout ce qui n'est pas reconnu retombe
 * sur {@link TypeDemande#AUTRE} — on ne laisse jamais un type inconnu casser le flux.
 */
public final class TypeDemandeMapper {

    private TypeDemandeMapper() {
    }

    public static TypeDemande from(String raw) {
        if (raw == null || raw.isBlank()) {
            return TypeDemande.AUTRE;
        }
        String norm = raw.trim();
        // 1. correspondance exacte sur le nom d'enum ou le libelle
        for (TypeDemande t : TypeDemande.values()) {
            if (t.name().equalsIgnoreCase(norm) || t.getLibelle().equalsIgnoreCase(norm)) {
                return t;
            }
        }
        // 2. correspondance partielle (l'agent a pu repondre une variante)
        String lower = norm.toLowerCase();
        for (TypeDemande t : TypeDemande.values()) {
            if (lower.contains(t.name().toLowerCase())
                    || t.getLibelle().toLowerCase().contains(lower)) {
                return t;
            }
        }
        return TypeDemande.AUTRE;
    }
}
