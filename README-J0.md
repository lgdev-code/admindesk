# AdminDesk — `feat/j0-base`

> **Formation IFAP – IA dans les Développements**
> Branche de départ : CRUD pur, zéro IA.

---

## Stack

| Composant     | Version  |
|---------------|----------|
| Java          | 25       |
| Spring Boot   | 3.5.12   |
| Spring AI     | —        |
| Base de données | H2 in-memory |
| Templates     | Thymeleaf + Bootstrap 5 |

---

## Démarrage rapide

```bash
# Cloner et se positionner sur la branche
git checkout feat/j0-base

# Lancer l'application
./mvnw spring-boot:run
```

L'application est disponible sur `http://localhost:8080`.

---

## URLs utiles

### Interface web (Thymeleaf — package `web/`)
| URL | Description |
|-----|-------------|
| `http://localhost:8080/demandes` | Liste des demandes |
| `http://localhost:8080/demandes/dashboard` | Tableau de bord |
| `http://localhost:8080/demandes/nouveau` | Créer une demande |

### API REST JSON (package `api/`) — c'est ici globalement que vous travaillez !
| URL | Description |
|-----|-------------|
| `http://localhost:8080/api/v1/demandes` | Liste paginée (GET) / Créer (POST) |
| `http://localhost:8080/api/v1/demandes/{id}` | Détail (GET) / Modifier (PUT) / Supprimer (DELETE) |
| `http://localhost:8080/api/v1/demandes/ref/{ref}` | Recherche par référence (GET) |
| `http://localhost:8080/api/v1/demandes/stats` | Statistiques JSON (GET) |

### Outils de développement
| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | **Swagger UI** — testez l'API REST directement depuis le navigateur |
| `http://localhost:8080/v3/api-docs` | Spécification OpenAPI (JSON) |
| `http://localhost:8080/h2-console` | Console H2 (JDBC URL: `jdbc:h2:mem:admindesk`) |
| `http://localhost:8080/actuator/health` | Health check |

---

## Structure du projet

```
src/main/java/fr/lgdev/admindesk/
├── AdminDeskApplication.java
├── api/                            ← REST JSON — vous travaillez ici
│   ├── apidoc/
│   │   └── DemandeApiDoc.java      # Contrat OpenAPI (interface)
│   ├── DemandeRestController.java  # Implémentation
│   └── RestExceptionHandler.java   # Erreurs REST (ProblemDetail)
├── config/
│   └── OpenApiConfig.java          # Configuration Swagger UI
├── web/                            ← Interface Thymeleaf — principalement à but pédagogique
│   ├── DemandeController.java
│   ├── HomeController.java
│   └── GlobalExceptionHandler.java
├── domain/
│   ├── Demande.java
│   ├── TypeDemande.java
│   ├── StatutDemande.java
│   └── Priorite.java
├── dto/
│   ├── DemandeFormDTO.java
│   └── DemandeResponseDTO.java
├── repository/
│   └── DemandeRepository.java
└── service/
    └── DemandeService.java
```

---

## Choix techniques simplifiés

> **⚠ Simplification volontaire — objets de domaine dans les vues Thymeleaf**
>
> Le package `web/` passe des objets `Demande` (entités JPA) directement aux templates Thymeleaf.
> C'est acceptable ici car le modèle est plat (pas de relations lazy) et les vues sont en lecture seule.
>
> En contexte professionnel, on introduirait un DTO dédié à la vue pour découpler
> le modèle de persistance de la couche de présentation et éviter tout risque de
> `LazyInitializationException` dès l'apparition de relations `@OneToMany`/`@ManyToOne` ou encore d'exposer 
> des champs internes à la vue.

---

## Lancer les tests

```bash
./mvnw test
```

---

## Ce que vous allez construire sur cette base

| Branche              | Contenu |
|----------------------|---------|
| `feat/j1-morning`    | Premier appel LLM — bouton "Résumer" |
| `feat/j1-afternoon`  | Prompt structuré + protocole de vérification |
| `feat/j2-morning`    | Assistant rédaction complet (3 fonctions IA) |
| `feat/j2-afternoon`  | Garde-fous, quotas, masquage, tableau de bord coûts |
| `feat/j3-morning`    | Pipeline RAG, embeddings, pgvector |
| `feat/j3-afternoon`  | Tests, observabilité, déploiement |
| `solution`           | Code complet — défis résolus |

---

## Défi express J0 (15 min)

Avant d'aller plus loin, explorez l'application :

1. Créez une demande via le formulaire
2. Appelez `GET /api/v1/demandes` avec curl ou Postman
3. Appelez `GET /api/v1/demandes/stats`
4. Identifiez dans le code où vous brancheriez un appel LLM sur la fiche détail

**Question à discuter** : Quels champs de `Demande` enverriez-vous au LLM ? Tous ? Lesquels exclure et pourquoi ?
