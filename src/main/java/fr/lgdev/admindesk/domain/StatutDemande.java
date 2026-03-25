package fr.lgdev.admindesk.domain;

public enum StatutDemande {

    EN_ATTENTE("En attente", "warning"),
    EN_COURS("En cours de traitement", "info"),
    TRAITE("Traitée", "success"),
    REJETE("Rejetée", "danger"),
    EN_ATTENTE_PIECES("En attente de pièces", "secondary");

    private final String libelle;
    private final String badgeCss;

    StatutDemande(String libelle, String badgeCss) {
        this.libelle  = libelle;
        this.badgeCss = badgeCss;
    }

    public String getLibelle()  { return libelle;  }
    public String getBadgeCss() { return badgeCss; }
}
