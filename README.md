# AdminDesk — Projet fil rouge

> **Formation IFAP – IA dans les Développements**

> **Branche actuelle : `feat/d1-tp2-prompt`**
> État du code après le **TP2 de la D1** : le prompt de `AIService.summarize()` est désormais
> structuré (R/C/T/F/Co, system/user séparés, format 3 lignes, refus explicite) et la temperature
> est à 0 (reproductibilité). Sert aussi de **base de départ pour le TP3**.

---

## Ce qui a changé depuis `feat/d1-tp1-naive`

| Fichier | Changement |
|---|---|
| `service/ai/AIService.java` | Prompt réécrit : system + user séparés, R/C/T/F/Co, format strict 3 lignes, refus explicite |
| `application.properties` | `temperature` passée de `0.7` à `0.0` |

Aucun autre fichier modifié — l'architecture (service, controllers, template) est inchangée depuis le TP1.

---

## Démarrage

```bash
export ANTHROPIC_API_KEY=sk-ant-...
git checkout feat/d1-tp2-prompt
./mvnw spring-boot:run
```

Testez le contraste avec le TP1 :
- `POST /api/v1/demandes/1/summarize` → 3 lignes Objet/Urgence/Action, stables sur 5 appels
- `POST /api/v1/demandes/12/summarize` → refus explicite (demande hors périmètre)

---

## Cycle pédagogique D1 — 3 TPs cumulatifs

| TP | Sujet | Base départ | Branche solution |
|---|---|---|---|
| TP1 | Premier appel LLM — prompt naïf | `feat/d1-base` | `feat/d1-tp1-naive` |
| **TP2** | Prompt structuré R/C/T/F/Co + temperature 0 | `feat/d1-tp1-naive` | **`feat/d1-tp2-prompt` ← vous êtes ici** |
| TP3 | Industrialiser : `call()` privée, logs, `AIServiceException`, ProblemDetail | `feat/d1-tp2-prompt` | `feat/d1-tp3-final` |

**Principe :** branche solution du TP en cours = base de départ du TP suivant. Branches cumulatives.
