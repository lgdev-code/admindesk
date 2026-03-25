package fr.lgdev.admindesk.domain;

public enum Priorite {

    BASSE("Basse", "success", 1),
    NORMALE("Normale", "primary", 2),
    HAUTE("Haute", "warning", 3),
    URGENTE("Urgente", "danger", 4);

    private final String libelle;
    private final String badgeCss;
    private final int niveau;

    Priorite(String libelle, String badgeCss, int niveau) {
        this.libelle  = libelle;
        this.badgeCss = badgeCss;
        this.niveau   = niveau;
    }

    public String getLibelle()  { return libelle;  }
    public String getBadgeCss() { return badgeCss; }
    public int    getNiveau()   { return niveau;   }
}
