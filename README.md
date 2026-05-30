# AdminDesk — Projet fil rouge

> **Formation IFAP – IA dans les Développements**
> Spring Boot · Spring AI · Anthropic

Application de gestion de demandes administratives, enrichie progressivement
avec des fonctionnalités IA au fil des **5 demi-journées** de formation.

> **Branche actuelle : `feat/d1-base`** — point de départ de la D1.
> **CRUD pur, ZÉRO IA.** Aucune dépendance Spring AI, aucun bean, aucun champ IA.
> Tout l'aspect IA (infrastructure ET code applicatif) sera construit par vous au fil des TPs.

---

## Ce qui est en place

Application métier complète, sans aucune trace d'intelligence artificielle :

- Gestion des demandes : liste, détail, création, édition, suppression
- API REST JSON + interface web Thymeleaf
- Tableau de bord avec statistiques métier (par statut, type, priorité)
- Base H2 in-memory peuplée via `data.sql`
- Documentation API via Swagger UI

**Il n'y a aucune dépendance Spring AI dans le `pom.xml`, aucun bean `ChatClient`,
aucun champ `resumeIa`.** Vous allez tout ajouter au TP1.

---

## Stack

| Composant | Version |
|---|---|
| Java | 25 |
| Spring Boot | 3.5.12 |
| Base de données | H2 in-memory |
| Templates | Thymeleaf + Bootstrap 5 |
| Doc API | springdoc-openapi (Swagger UI) |

> Spring AI (1.1.0) et le modèle Claude Haiku 4.5 seront ajoutés au TP1.

---

## Démarrage rapide

```bash
git checkout feat/d1-base
mvn spring-boot:run
```

L'application démarre sur http://localhost:8080.
Pas besoin de clé API à ce stade — il n'y a pas encore d'appel LLM.

---

## URLs utiles

### Interface web (Thymeleaf)
| URL | Description |
|---|---|
| http://localhost:8080/demandes | Liste des demandes |
| http://localhost:8080/demandes/dashboard | Tableau de bord |
| http://localhost:8080/demandes/nouveau | Créer une demande |

### API REST JSON
| URL | Description |
|---|---|
| `GET /api/v1/demandes` | Liste paginée |
| `POST /api/v1/demandes` | Créer |
| `GET /api/v1/demandes/{id}` | Détail |
| `PUT /api/v1/demandes/{id}` | Modifier |
| `DELETE /api/v1/demandes/{id}` | Supprimer |
| `GET /api/v1/demandes/stats` | Statistiques métier |

### Outils
| URL | Description |
|---|---|
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/h2-console | Console H2 (`jdbc:h2:mem:admindesk`) |
| http://localhost:8080/actuator/health | Health check |

---

## Cycle pédagogique D1 — 3 TPs cumulatifs

| TP | Sujet | Base départ | Branche solution |
|---|---|---|---|
| **TP1** | Brancher Spring AI + premier appel LLM (prompt naïf) | **`feat/d1-base` ← vous êtes ici** | `feat/d1-tp1-naive` |
| **TP2** | Améliorer le prompt avec R/C/T/F/Co + séparation system/user | `feat/d1-tp1-naive` | `feat/d1-tp2-prompt` |
| **TP3** | Industrialiser : `call()` privée, logs, `AIServiceException`, ProblemDetail | `feat/d1-tp2-prompt` | `feat/d1-tp3-final` |

Au **TP1**, vous ajouterez d'abord l'infrastructure Spring AI (dépendance Maven, bean `ChatClient`,
configuration Anthropic, champ `resumeIa`), puis le code applicatif (service, endpoint, action web, template).

**Principe :** la branche solution du TP en cours = base de départ du TP suivant. Branches **cumulatives**.
