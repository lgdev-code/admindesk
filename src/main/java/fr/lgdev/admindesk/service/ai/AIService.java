package fr.lgdev.admindesk.service.ai;

import fr.lgdev.admindesk.domain.Demande;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.anthropic.api.AnthropicCacheOptions;
import org.springframework.ai.anthropic.api.AnthropicCacheStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * AdminDesk — Service IA.
 *
 * État TP6 (D2) — forme finale de call() : quota -> masquage RGPD -> appel LLM -> comptage.
 *  - QuotaService.check() avant l'appel (peut lever 429) ;
 *  - InputSanitizer.sanitize() masque le user ;
 *  - QuotaService.recordUsage() APRÈS succès uniquement (jamais sur échec).
 *
 * État TP7 (D3) — les 4 fonctions IA sont @Cacheable (une région de cache par fonction :
 *  summaries / reformulations / infos-manquantes / categories), clé = hash du contenu.
 *  Un cache hit court-circuite la méthode : zéro appel LLM, quota intact.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final ChatClient chatClient;
    private final InputSanitizer sanitizer;
    private final QuotaService quotas;

    /**
     * Options d'appel demandant à Anthropic de mettre en cache le SYSTEM prompt
     * (breakpoint cache_control). Le system étant identique entre appels d'une même
     * fonction IA, le 1er appel l'ÉCRIT en cache, les suivants (< 5 min) le LISENT
     * à ~0,1x du prix. ⚠️ Anthropic n'active le cache que si le préfixe atteint le
     * minimum du modèle (4096 tokens pour Haiku 4.5) : en dessous, aucun effet.
     */
    private static final AnthropicChatOptions CACHE_SYSTEM = AnthropicChatOptions.builder()
            .cacheOptions(AnthropicCacheOptions.builder()
                    .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                    .build())
            .build();

    @Cacheable(value = "summaries",
               key = "T(fr.lgdev.admindesk.util.Hash).sha256(#demande.description)")
    public String summarize(Demande demande, Long agentId) {

        String system = """
                Tu es un agent administratif expert.
                Tu traites des demandes de citoyens calédoniens adressées à une collectivité.

                Tu réponds TOUJOURS exactement dans ce format, sur 3 lignes,
                sans introduction ni conclusion :
                Objet : <résumé de la demande en une phrase>
                Urgence : <Oui ou Non, suivi d'une courte justification>
                Action suggérée : <la prochaine étape concrète pour l'agent>

                Contraintes :
                - Réponds uniquement en français.
                - N'invente AUCUNE information absente de la demande.
                - Reste neutre et professionnel.
                - Si la demande ne relève pas d'un service administratif
                  ou si elle est inexploitable, écris exactement :
                  "Objet : demande hors périmètre ou inexploitable"
                  et laisse les deux autres lignes vides.
                """;

        String user = """
                Analyse et résume la demande suivante :

                %s
                """.formatted(demande.getDescription());

        return call(system, user, agentId);
    }

    @Cacheable(value = "reformulations",
               key = "T(fr.lgdev.admindesk.util.Hash).sha256(#demande.description)")
    public String reformulate(Demande demande, Long agentId) {

        String system = """
                Tu es un agent administratif.
                Reformule en français administratif neutre.
                Garde le sens, change le ton.
                N'invente rien, n'ajoute rien.
                Si la demande est déjà formelle, retourne-la quasi à l'identique.
                """;

        String user = """
                Reformule la demande suivante :

                %s
                """.formatted(demande.getDescription());

        return call(system, user, agentId);
    }

    @Cacheable(value = "infos-manquantes",
               key = "T(fr.lgdev.admindesk.util.Hash).sha256(#demande.description)")
    public String detectMissingInfo(Demande demande, Long agentId) {

        String system = """
                Tu es un agent administratif.
                Liste les informations manquantes ou imprécises nécessaires pour instruire la demande.
                Réponds UNIQUEMENT par des questions courtes, une par ligne, chacune préfixée par "Q: ".
                N'invente rien, ne reformule pas la demande.
                Si la demande est complète, réponds exactement : "Aucune information manquante."
                """;

        String user = """
                Identifie les informations manquantes dans la demande suivante :

                %s
                """.formatted(demande.getDescription());

        return call(system, user, agentId);
    }

    @Cacheable(value = "categories",
               key = "T(fr.lgdev.admindesk.util.Hash).sha256(#demande.description)")
    public String categorize(Demande demande, Long agentId) {

        // Prompt système volumineux (> 4096 tokens) : condition pour que le prompt caching
        // Anthropic (stratégie SYSTEM_ONLY) s'active réellement. Contenu 100 % statique =>
        // préfixe byte-identique entre appels => cache réutilisable. Cf. logCacheUsage().
        String system = """
                Tu es un agent administratif d'une collectivité de Nouvelle-Calédonie, spécialisé dans le tri et l'orientation des demandes citoyennes reçues par le guichet numérique. Ta mission est d'attribuer à chaque demande la catégorie la plus adaptée parmi la liste fermée ci-dessous, et elle seule. Tu ne dois jamais inventer de catégorie hors de cette liste, ni en combiner plusieurs. En cas de doute entre deux catégories, applique les règles de désambiguïsation fournies plus bas.

                Réponds TOUJOURS exactement dans ce format, sans aucune phrase d'introduction ni de conclusion :
                Catégorie : <un libellé exact de la liste autorisée>
                Justification : <une phrase courte expliquant le critère décisif>

                ============================================================
                LISTE AUTORISÉE ET BARÈME DÉTAILLÉ
                ============================================================

                1) Urbanisme & Construction
                Définition : toute demande relative au droit des sols, à la construction, à la rénovation, à l'extension ou à la démolition d'un bien bâti, ainsi qu'aux règles d'aménagement applicables à une parcelle.
                Relèvent de cette catégorie : les demandes de permis de construire, les déclarations préalables de travaux, les certificats d'urbanisme, les questions sur le plan local d'urbanisme (PLU) ou le zonage d'une parcelle, les projets d'extension (véranda, garage, surélévation), les ravalements de façade soumis à déclaration, les clôtures, les piscines, les abris de jardin, les changements de destination d'un local.
                Ne relèvent PAS de cette catégorie : les autorisations d'occupation temporaire de la voie publique (voir Permis & Autorisations), l'entretien des routes et trottoirs (voir Voirie & Espace public), les nuisances liées à un chantier voisin lorsqu'il s'agit d'une plainte environnementale (voir Environnement).
                Mots-clés déclencheurs : permis de construire, déclaration préalable, PLU, zonage, parcelle, cadastre, extension, surélévation, véranda, clôture, piscine, ravalement, certificat d'urbanisme, Cerfa 13406, Cerfa 13703.
                Exemple : « Je souhaite construire une véranda de 25 m² sur la façade de ma maison » => Urbanisme & Construction.

                2) Aide sociale
                Définition : toute demande visant à obtenir un soutien financier, matériel ou humain destiné à une personne en situation de fragilité (âge, handicap, précarité, isolement).
                Relèvent de cette catégorie : les demandes d'allocation personnalisée d'autonomie (APA), les aides au logement, les demandes de secours d'urgence, l'accompagnement des personnes âgées ou handicapées, le portage de repas, les demandes de domiciliation pour les personnes sans domicile stable, les aides à la garde d'enfant à caractère social, les demandes d'assistante sociale.
                Ne relèvent PAS de cette catégorie : l'inscription d'un enfant à l'école ou à la cantine, même dans un contexte de difficulté financière (voir Inscription scolaire), les demandes d'état civil comme un acte de naissance nécessaire à la constitution d'un dossier (voir État civil).
                Mots-clés déclencheurs : APA, aide sociale, allocation, secours, précarité, personne âgée, handicap, portage de repas, assistante sociale, domiciliation, CCAS, RSA, aide au logement.
                Exemple : « Je demande l'APA pour ma mère de 84 ans qui perd son autonomie » => Aide sociale.

                3) Inscription scolaire
                Définition : toute demande relative à la scolarité d'un enfant au sein des établissements gérés ou rattachés à la collectivité (écoles maternelles et primaires), ainsi qu'aux services périscolaires.
                Relèvent de cette catégorie : l'inscription ou la radiation d'un élève, la demande de dérogation de secteur scolaire, l'inscription à la cantine scolaire, l'inscription à la garderie ou à l'accueil périscolaire, les demandes de changement d'école, les questions sur les fournitures ou le règlement intérieur d'une école.
                Ne relèvent PAS de cette catégorie : les aides financières à caractère social liées à la garde d'enfant (voir Aide sociale), les travaux ou l'entretien d'un bâtiment scolaire (voir Urbanisme & Construction ou Voirie selon la nature).
                Mots-clés déclencheurs : inscription scolaire, école, maternelle, primaire, cantine, garderie, périscolaire, dérogation, secteur scolaire, radiation, changement d'école.
                Exemple : « Je souhaite inscrire mon fils en CP à l'école Jean Jaurès pour la rentrée » => Inscription scolaire.

                4) État civil
                Définition : toute demande relative aux actes et registres d'état civil, à l'identité juridique d'une personne et aux événements de la vie civile.
                Relèvent de cette catégorie : les demandes d'acte de naissance, de mariage ou de décès, les livrets de famille, les déclarations de naissance ou de décès, les demandes de célébration de mariage ou de PACS, les changements de nom ou de prénom, les rectifications d'acte, les demandes de reconnaissance, les recensements citoyens.
                Ne relèvent PAS de cette catégorie : les demandes de titre d'identité comme la carte nationale d'identité ou le passeport lorsqu'elles relèvent d'une autorisation administrative distincte (voir Permis & Autorisations), les aides sociales nécessitant un acte comme pièce justificative (voir Aide sociale).
                Mots-clés déclencheurs : acte de naissance, acte de mariage, acte de décès, livret de famille, PACS, reconnaissance, changement de nom, changement de prénom, rectification d'acte, recensement citoyen.
                Exemple : « J'ai besoin d'une copie intégrale de mon acte de naissance pour un dossier de retraite » => État civil.

                5) Permis & Autorisations
                Définition : toute demande d'autorisation administrative ponctuelle qui ne relève ni de l'urbanisme, ni de l'état civil, et qui encadre un usage particulier de l'espace, d'un équipement ou d'un droit.
                Relèvent de cette catégorie : les autorisations d'occupation temporaire du domaine public (terrasse, échafaudage, benne, déménagement), les permis de stationnement, les autorisations de débit de boissons temporaire, les autorisations pour l'organisation d'une manifestation ou d'un événement sur la voie publique, les autorisations de battue ou de brûlage, les demandes de carte de stationnement.
                Ne relèvent PAS de cette catégorie : les permis de construire et déclarations de travaux (voir Urbanisme & Construction), les signalements d'un problème sur la voirie (voir Voirie & Espace public).
                Mots-clés déclencheurs : autorisation, occupation du domaine public, terrasse, échafaudage, benne, déménagement, permis de stationnement, débit de boissons, manifestation, événement, brûlage, battue.
                Exemple : « Je voudrais installer une terrasse devant mon commerce durant l'été » => Permis & Autorisations.

                6) Voirie & Espace public
                Définition : toute demande relative à l'entretien, la réparation, la sécurité ou l'aménagement des voies de circulation et des espaces publics ouverts.
                Relèvent de cette catégorie : les signalements de nids-de-poule, de trottoirs dégradés, d'éclairage public en panne, de panneaux de signalisation manquants ou endommagés, de problèmes de stationnement gênant, de mobilier urbain cassé, de canalisations d'eaux pluviales bouchées, de marquage au sol effacé, de demandes de ralentisseurs ou de sécurisation d'un carrefour.
                Ne relèvent PAS de cette catégorie : les autorisations d'occupation de la voie publique (voir Permis & Autorisations), les pollutions ou dépôts sauvages relevant de l'environnement (voir Environnement), les travaux de construction privés (voir Urbanisme & Construction).
                Mots-clés déclencheurs : nid-de-poule, trottoir, éclairage public, lampadaire, panneau, signalisation, stationnement gênant, mobilier urbain, marquage au sol, ralentisseur, carrefour, chaussée, caniveau.
                Exemple : « Il y a un trou énorme dans la chaussée rue du Commerce, c'est dangereux » => Voirie & Espace public.

                7) Environnement
                Définition : toute demande relative à la protection de l'environnement, à la propreté publique, aux nuisances et à la gestion des déchets et des espaces naturels.
                Relèvent de cette catégorie : les signalements de dépôts sauvages d'ordures, les nuisances sonores, les pollutions de l'eau ou de l'air, les problèmes de collecte des déchets, les demandes relatives aux espaces verts et à leur entretien, les signalements d'animaux errants ou nuisibles, les questions sur le compostage, le tri sélectif, les demandes de coupe ou d'élagage d'arbres relevant du domaine public.
                Ne relèvent PAS de cette catégorie : les nids-de-poule et l'éclairage relevant de l'entretien courant de la voirie (voir Voirie & Espace public), les autorisations de brûlage (voir Permis & Autorisations).
                Mots-clés déclencheurs : dépôt sauvage, ordures, déchets, nuisance sonore, pollution, collecte, tri sélectif, compost, espaces verts, élagage, animaux errants, propreté, décharge.
                Exemple : « Des ordures sont déposées illégalement au bout de mon chemin depuis des semaines » => Environnement.

                8) Autre
                Définition : toute demande qui ne correspond clairement à aucune des sept catégories précédentes, ou dont l'objet est trop imprécis pour être rattaché avec certitude.
                Relèvent de cette catégorie : les demandes d'information générale, les réclamations non catégorisables, les félicitations ou remerciements, les demandes internes sans rapport avec un service identifié.
                Règle : n'utilise « Autre » qu'en dernier recours, après avoir écarté explicitement les sept autres catégories.
                Exemple : « Je souhaite féliciter vos équipes pour la qualité de l'accueil » => Autre.

                ============================================================
                RÈGLES DE DÉSAMBIGUÏSATION
                ============================================================
                - Voirie vs Environnement : si le problème porte sur l'état physique de la chaussée, du trottoir, de l'éclairage ou de la signalisation, choisis Voirie. S'il porte sur la propreté, les déchets, une pollution ou une nuisance, choisis Environnement.
                - Urbanisme vs Permis : si la demande concerne le bâti et le droit des sols (construire, agrandir, démolir), choisis Urbanisme. Si elle concerne un usage temporaire de l'espace ou un droit ponctuel, choisis Permis & Autorisations.
                - Aide sociale vs Inscription scolaire : dès qu'il s'agit d'inscrire un enfant à l'école, à la cantine ou à la garderie, choisis Inscription scolaire, même si le contexte est social. Réserve Aide sociale aux soutiens financiers, matériels ou humains directs.
                - État civil vs Aide sociale : un acte demandé POUR constituer un dossier social relève d'État civil (c'est la nature de l'acte qui prime, pas sa finalité).
                - En cas d'égalité stricte entre deux catégories après application des règles ci-dessus, choisis celle dont les mots-clés apparaissent le plus explicitement dans la demande.

                ============================================================
                EXEMPLES DE RÉFÉRENCE
                ============================================================
                Demande : « Le lampadaire devant le 17 rue des Acacias est en panne depuis trois semaines. » => Voirie & Espace public (équipement d'éclairage public défaillant).
                Demande : « Je veux déposer une benne sur le trottoir pendant mes travaux la semaine prochaine. » => Permis & Autorisations (occupation temporaire du domaine public).
                Demande : « Comment inscrire ma fille à la cantine de l'école maternelle ? » => Inscription scolaire (service périscolaire).
                Demande : « Je demande un extrait d'acte de mariage de 1998. » => État civil (acte d'état civil).
                Demande : « Un voisin brûle ses déchets verts tous les dimanches, la fumée est irrespirable. » => Environnement (nuisance et pollution de l'air).
                Demande : « Je voudrais agrandir ma maison avec un étage supplémentaire. » => Urbanisme & Construction (extension du bâti soumise à autorisation).
                Demande : « Ma mère de 88 ans ne peut plus faire ses courses, quelles aides existent ? » => Aide sociale (soutien aux personnes âgées en perte d'autonomie).
                Demande : « Des poubelles débordent en permanence au square des Cocotiers. » => Environnement (propreté publique et collecte).
                Demande : « Le passage piéton devant l'école est complètement effacé. » => Voirie & Espace public (marquage au sol à refaire).
                Demande : « Je souhaite organiser une brocante sur la place du marché le mois prochain. » => Permis & Autorisations (manifestation sur la voie publique).

                ============================================================
                EXEMPLES SUPPLÉMENTAIRES ET CAS LIMITES
                ============================================================
                Demande : « Le panneau stop au croisement de la rue Verte a été arraché après un accident. » => Voirie & Espace public (signalisation routière endommagée).
                Demande : « Je veux signaler un chien errant agressif qui rôde dans le quartier depuis plusieurs jours. » => Environnement (animal errant, salubrité publique).
                Demande : « Comment obtenir une autorisation pour vendre des boissons lors de la fête du quartier ? » => Permis & Autorisations (débit de boissons temporaire).
                Demande : « Je dois déclarer la naissance de mon fils né la semaine dernière. » => État civil (déclaration de naissance).
                Demande : « Je voudrais poser une clôture de 1,80 m autour de mon terrain. » => Urbanisme & Construction (clôture soumise à déclaration préalable).
                Demande : « Mon père handicapé a besoin d'un accompagnement à domicile plusieurs fois par semaine. » => Aide sociale (accompagnement des personnes en situation de handicap).
                Demande : « Je souhaite une dérogation pour scolariser ma fille hors de notre secteur. » => Inscription scolaire (dérogation de secteur scolaire).
                Demande : « L'eau stagne dans le caniveau et déborde à chaque pluie devant chez moi. » => Voirie & Espace public (évacuation des eaux pluviales sur la voie).
                Demande : « Une entreprise a déversé des gravats dans le ruisseau communal. » => Environnement (pollution d'un cours d'eau).
                Demande : « Je veux réserver la salle des fêtes pour un mariage privé. » => Permis & Autorisations (réservation d'un équipement public).
                Demande : « J'ai besoin d'un certificat d'urbanisme avant d'acheter un terrain. » => Urbanisme & Construction (information sur les règles d'urbanisme d'une parcelle).
                Demande : « Les containers de tri du quartier ne sont plus ramassés depuis quinze jours. » => Environnement (dysfonctionnement de la collecte des déchets).
                Demande : « Je souhaite me marier à la mairie au mois de juin. » => État civil (célébration de mariage).
                Demande : « Un lampadaire clignote toute la nuit et empêche les riverains de dormir. » => Voirie & Espace public (dysfonctionnement de l'éclairage public).
                Demande : « Quelles aides puis-je obtenir pour payer mon loyer, je viens de perdre mon emploi ? » => Aide sociale (aide au logement en situation de précarité).

                Rappel final : ne produis que les deux lignes du format demandé (Catégorie, Justification). N'ajoute jamais de commentaire, de salutation, ni de texte hors de ce format. Si la demande est vide, inexploitable ou hors du champ d'un service administratif, réponds « Catégorie : Autre » avec une justification indiquant que la demande est hors périmètre.
                """;

        String user = """
                Catégorise la demande suivante :

                %s
                """.formatted(demande.getDescription());

        return call(system, user, agentId);
    }

    /**
     * Point d'appel unique vers le LLM. Centralise chrono, log et gestion d'erreur.
     * RGPD : on ne logue JAMAIS le contenu des prompts, seulement la latence et l'agent.
     */
    private String call(String system, String user, Long agentId) {
        quotas.check(agentId);                          // garde-fou quota (peut lever 429)
        long t0 = System.nanoTime();
        String safeUser = sanitizer.sanitize(user);   // masquage RGPD avant envoi
        try {
            ChatResponse response = chatClient.prompt()
                    .system(system)
                    .user(safeUser)
                    .options(CACHE_SYSTEM)                 // active le cache_control sur le system
                    .call()
                    .chatResponse();
            String content = response.getResult().getOutput().getText();
            log.info("LLM call OK in {} ms — agent={}",
                    (System.nanoTime() - t0) / 1_000_000, agentId);
            logCacheUsage(response, agentId);              // trace hit/miss du cache Anthropic
            quotas.recordUsage(agentId, estimateTokens(content));  // APRÈS succès uniquement
            return content;
        } catch (Exception e) {
            log.error("LLM call failed after {} ms — agent={}",
                    (System.nanoTime() - t0) / 1_000_000, agentId, e);
            throw new AIServiceException("Échec appel IA", e);
        }
    }

    /**
     * Trace l'usage du cache Anthropic pour vérifier qu'on l'atteint bien.
     *  - cacheReadInputTokens > 0     -> HIT  (tokens servis depuis le cache, ~0,1x du prix)
     *  - cacheCreationInputTokens > 0 -> ÉCRITURE (cache créé, hit au prochain appel < 5 min)
     *  - les deux à 0                 -> MISS (préfixe < minimum du modèle, cf. 4096 pour Haiku 4.5)
     */
    private void logCacheUsage(ChatResponse response, Long agentId) {
        if (response == null || response.getMetadata().getUsage() == null
                || !(response.getMetadata().getUsage().getNativeUsage() instanceof AnthropicApi.Usage u)) {
            return;
        }
        Integer created = u.cacheCreationInputTokens();
        Integer read    = u.cacheReadInputTokens();
        log.info("Anthropic usage — input={}, output={}, cacheCreation={}, cacheRead={} (agent={})",
                u.inputTokens(), u.outputTokens(), created, read, agentId);

        if (read != null && read > 0) {
            log.info("CACHE HIT — {} tokens servis depuis le cache (agent={})", read, agentId);
        } else if (created != null && created > 0) {
            log.info("CACHE ÉCRIT — {} tokens mis en cache ; HIT au prochain appel < 5 min (agent={})",
                    created, agentId);
        } else {
            log.warn("PAS DE CACHE — préfixe system trop court (< 4096 tokens pour Haiku 4.5) : "
                    + "cache_control ignoré par Anthropic (agent={})", agentId);
        }
    }

    /** Estimation grossière suffisante pour le décompte du quota (~1 token pour 4 caractères). */
    private int estimateTokens(String text) {
        return text == null ? 0 : text.length() / 4;
    }
}
