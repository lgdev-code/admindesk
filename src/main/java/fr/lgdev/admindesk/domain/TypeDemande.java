package fr.lgdev.admindesk.domain;

public enum TypeDemande {

    URBANISME("Urbanisme & Construction"),
    AIDE_SOCIALE("Aide sociale"),
    INSCRIPTION_SCOLAIRE("Inscription scolaire"),
    ETAT_CIVIL("État civil"),
    PERMIS("Permis & Autorisations"),
    VOIRIE("Voirie & Espace public"),
    ENVIRONNEMENT("Environnement"),
    AUTRE("Autre");

    private final String libelle;

    TypeDemande(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
