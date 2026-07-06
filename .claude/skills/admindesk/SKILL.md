---
description: Interroger les demandes citoyennes AdminDesk via les tools MCP admindesk
---

Réponds à la demande suivante : $ARGUMENTS

Pour y répondre, appelle exclusivement les tools MCP disponibles sous le préfixe `mcp__admindesk__*` (recherche, comptage, statistiques, détail par référence, demande la plus urgente, etc.).

Règles strictes :
- N'invente jamais de données : toute référence, statut, priorité, type, agent assigné ou contenu de demande doit provenir directement d'un résultat de tool `mcp__admindesk__*`.
- Si les tools disponibles ne permettent pas de répondre complètement, dis-le explicitement plutôt que de compléter par supposition.
- N'utilise aucune autre source (web, mémoire, fichiers locaux) pour répondre à cette demande.
