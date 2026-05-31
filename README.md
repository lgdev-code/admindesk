# AdminDesk — Projet fil rouge

> **Formation IFAP – IA dans les Développements**
> Spring Boot · Spring AI · Anthropic · pgvector

Application de gestion de demandes administratives enrichie progressivement
avec des fonctionnalités IA au fil des **5 demi-journées** de formation.

> **Branche actuelle : `feat/d1-tp2-prompt`**
> État du code après le **TP1 de la D1** (premier appel LLM avec prompt naïf).
> Cette branche sert également de **base de départ pour le TP2**.

---

## Cycle pédagogique D1 — 3 TPs cumulatifs

| TP | Sujet | Base départ | Branche solution |
|---|---|---|---|
| **TP1** | Premier appel LLM — prompt naïf, observation des défauts | `feat/d1-base` | **`feat/d1-tp1-naive` ← vous êtes ici** |
| **TP2** | Améliorer le prompt avec R/C/T/F/Co + séparation system/user | `feat/d1-tp1-naive` | `feat/d1-tp2-prompt` |
| **TP3** | Industrialiser : `call()` privée, logs, `AIServiceException`, ProblemDetail | `feat/d1-tp2-prompt` | `feat/d1-tp3-final` |

**Principe :** la branche solution du TP en cours = base de départ du TP suivant.
Les branches sont **cumulatives** — chacune contient tout ce que les précédentes contenaient + les ajouts du TP.

---
