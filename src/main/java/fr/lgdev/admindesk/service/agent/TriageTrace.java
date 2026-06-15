package fr.lgdev.admindesk.service.agent;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

/**
 * Collecteur, le temps d'une requete HTTP, de tous les outils que l'agent a decide
 * d'appeler — dans l'ordre. Permet d'AFFICHER le raisonnement de l'agent au stagiaire,
 * le meme effet « reveal console » que le masquage [NIR][TEL][IBAN] du TP5.
 *
 * <p>Le scope requete est sur ici parce que la boucle d'outils de {@code .call()}
 * s'execute de maniere SYNCHRONE, sur le thread de la requete. (Si un jour on passe en
 * {@code .stream()} ou en execution asynchrone, ce scope casse — voir notes formateur.)</p>
 */
@Component
@RequestScope
public class TriageTrace {

    private final List<String> appels = new ArrayList<>();

    /** Appele par chaque outil au moment ou l'agent le declenche. */
    public void log(String appel) {
        appels.add(appel);
    }

    public List<String> getAppels() {
        return List.copyOf(appels);
    }
}
