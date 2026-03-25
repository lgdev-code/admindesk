# AdminDesk — Projet fil rouge

> **Formation IFAP – IA dans les Développements**
> Spring Boot · Spring AI · Anthropic · pgvector

Application de gestion de demandes administratives enrichie progressivement
avec des fonctionnalités IA au fil des 3 jours de formation.

---

## Branches

| Branche | Contenu |
|---|---|
| `feat/j0-base` | CRUD pur — zéro IA |
| `feat/j1-morning` | Premier appel LLM — bouton "Résumer" |
| `feat/j1-afternoon` | Prompt structuré + protocole de vérification |
| `feat/j2-morning` | Assistant rédaction complet (3 fonctions IA) |
| `feat/j2-afternoon` | Garde-fous, quotas, masquage, tableau de bord coûts |
| `feat/j3-morning` | Pipeline RAG, embeddings, pgvector |
| `feat/j3-afternoon` | Tests, observabilité, déploiement |
| `solution` | Code complet — tous les défis résolus |
| `develop` | **Branche de travail participants** — mise à jour par le formateur au début de chaque demi-journée |

---

## Stack

| Composant | Version |
|---|---|
| Java | 25 |
| Spring Boot | 3.5.12 |
| Spring AI | 1.1.0 |
| LLM | `claude-haiku-4-5-20251001` (Anthropic) |
| Embeddings | `text-embedding-3-small` (OpenAI) |
| Base de données (J0–J2) | H2 in-memory |
| Base de données (J3) | PostgreSQL + pgvector (Neon) |

---

## Prérequis

- **JDK 25** — inclus dans le toolkit
- **Maven** — inclus via le wrapper `./mvnw`
- **IntelliJ IDEA** 2025.x recommandé
- Clés API fournies par le formateur le jour J

---

## Démarrage

```bash
git clone https://github.com/lgdev-code/admindesk.git
cd admindesk
git checkout develop

./mvnw spring-boot:run
```

L'application démarre sur [http://localhost:8080](http://localhost:8080).

---

## Configuration des clés API (à partir de J1)

Ne jamais committer de clés. Deux options :

**Variables d'environnement (recommandé)**

```bash
export ANTHROPIC_API_KEY=sk-ant-...
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

**Fichier local non versionné**

Créer `src/main/resources/application-local.properties` (ignoré par `.gitignore`) :

```properties
spring.ai.anthropic.api-key=sk-ant-...
spring.ai.openai.api-key=sk-...
```

Puis lancer avec :

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## URLs utiles (une fois l'application démarrée)

| URL | Description |
|---|---|
| [/demandes](http://localhost:8080/demandes) | Interface web |
| [/demandes/dashboard](http://localhost:8080/demandes/dashboard) | Tableau de bord |
| [/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Swagger UI |
| [/h2-console](http://localhost:8080/h2-console) | Console H2 (J0–J2) |
| [/actuator/health](http://localhost:8080/actuator/health) | Health check |

---

## Lancer les tests

```bash
./mvnw test
```

