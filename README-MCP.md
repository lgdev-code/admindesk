# AdminDesk — Serveur MCP

Ce module expose les demandes administratives d'AdminDesk à un client MCP
(**Claude Desktop**) via le **Model Context Protocol**. Claude peut ainsi interroger
directement l'application Spring Boot en langage naturel, par exemple :

> « Combien de demandes VOIRIE sont en cours ? Donne-moi le détail de la plus urgente. »

## Architecture

| Élément | Rôle |
|---|---|
| `mcp/DemandeMcpTools` | Les outils exposés (méthodes `@Tool`). Chaque méthode devient un outil appelable par Claude. |
| `mcp/McpConfig` | Déclare le bean `ToolCallbackProvider` qui enregistre les outils auprès du serveur MCP. **Sans lui, aucun outil n'est exposé.** |
| `spring-ai-starter-mcp-server-webmvc` | Le starter Spring AI qui publie le serveur MCP en transport **SSE** (Server-Sent Events) sur le port HTTP de l'application. |

Le serveur MCP est servi par l'application web elle-même :

- Endpoint SSE : `http://localhost:8080/sse`
- Endpoint messages : `http://localhost:8080/mcp/message`

### Outils disponibles

| Outil | Description |
|---|---|
| `searchDemandes` | Recherche des demandes par type / statut / priorité / texte libre. |
| `countDemandes` | Compte les demandes correspondant à des critères. |
| `getMostUrgentDemande` | Détail de la demande la plus urgente pour des critères donnés. |
| `getDemandeByReference` | Détail complet d'une demande à partir de sa référence. |
| `statsByType` / `statsByStatus` / `statsByPriority` | Statistiques agrégées. |

## Prérequis

- **Node.js** installé (fournit `npx`), utilisé pour le pont `mcp-remote`.
- **Claude Desktop** installé.
- L'application AdminDesk démarrée (voir ci-dessous).

## 1. Démarrer l'application

Le serveur MCP fonctionne en **profil par défaut** (base H2 en mémoire avec données de
test). Le profil `prod` n'est **pas** nécessaire : il ne sert qu'au RAG (pgvector).

```bash
# Variable requise : la clé Anthropic (le contexte ne démarre pas sans elle)
export ANTHROPIC_API_KEY=sk-ant-...

./mvnw spring-boot:run
```

Au démarrage, l'endpoint SSE doit être disponible sur `http://localhost:8080/sse`.

## 2. Configurer Claude Desktop

Claude Desktop ne sait lancer que des serveurs **stdio**. Comme AdminDesk expose le MCP
en **SSE/HTTP**, on utilise le pont [`mcp-remote`](https://www.npmjs.com/package/mcp-remote)
(via `npx`) pour relier les deux.

Ouvrir le fichier de configuration :

- Windows : `%APPDATA%\Claude\claude_desktop_config.json`
- Ou depuis l'app : **Menu → Fichier → Paramètres → Développeur → Modifier la configuration**.

Ajouter la section `admindesk` sous `mcpServers` :

```json
{
  "mcpServers": {
    "admindesk": {
      "command": "PATH_TO/npx.cmd",
      "args": [
        "-y",
        "mcp-remote",
        "http://localhost:8080/sse",
        "--transport",
        "sse-only"
      ],
       "env": {
          "PATH": "PATH_TO\\nodejs\\22.21.1;C:\\WINDOWS\\system32;C:\\WINDOWS"
       }
    }
  }
}
```

> **Remarque sur `command`**
> Si `npx` n'est pas dans le `PATH` vu par Claude Desktop, indiquez le chemin absolu
> vers `npx.cmd` (Windows) et ajoutez un bloc `env.PATH` pointant vers le dossier
> d'installation de Node.js. Pour retrouver ce chemin : `where npx` (Windows) /
> `which npx` (macOS/Linux).
>
> ```json
> "admindesk": {
>   "command": "<CHEMIN_ABSOLU>\\npx.cmd",
>   "args": ["-y", "mcp-remote", "http://localhost:8080/sse", "--transport", "sse-only"],
>   "env": {
>     "PATH": "<DOSSIER_NODEJS>;C:\\WINDOWS\\system32;C:\\WINDOWS"
>   }
> }
> ```

L'option `--transport sse-only` force `mcp-remote` à utiliser SSE directement (sinon il
tente d'abord le *Streamable HTTP*, ce qui peut ralentir ou faire échouer le handshake
avec un serveur qui n'expose que `/sse`).

## 3. Redémarrer et vérifier

1. **Quitter complètement** Claude Desktop (icône barre système → *Quitter*, pas juste
   fermer la fenêtre).
2. Relancer Claude Desktop (l'application AdminDesk doit tourner).
3. Cliquer l'icône outils : le serveur `admindesk` et ses outils doivent apparaître.
4. Tester : *« Combien de demandes VOIRIE sont en cours ? Donne-moi le détail de la plus urgente. »*

### Vérification sans Claude (recommandé)

Pour valider le serveur indépendamment du client, utiliser l'inspecteur MCP :

```bash
npx @modelcontextprotocol/inspector
```

Transport **SSE**, URL `http://localhost:8080/sse` → *Connect* → *List Tools* → appeler
un outil à la main. Si l'inspecteur répond, le serveur est OK et un éventuel problème se
situe côté pont `mcp-remote` / Claude Desktop.

## Dépannage

| Symptôme | Cause probable / solution |
|---|---|
| Le serveur `admindesk` n'apparaît pas / est en erreur | L'application n'est pas démarrée, ou le port n'est pas 8080. Vérifier `http://localhost:8080/sse`. |
| `Request timed out` puis `notifications/cancelled` dans les logs serveur | Le client a annulé faute de réponse à temps. Le `WARN` « No handler registered for notification method: notifications/cancelled » est **bénin**. Vérifier d'abord le serveur avec l'inspecteur, puis forcer `--transport sse-only` côté `mcp-remote`. |
| `npx` introuvable au lancement | Utiliser le chemin absolu vers `npx.cmd` + `env.PATH` (voir remarque ci-dessus). |
| Connexion SSE coupée après inactivité | Activer un keep-alive côté serveur : `spring.ai.mcp.server.keep-alive-interval=30s` dans `application.properties`. |

Logs du pont côté Claude Desktop : `%APPDATA%\Claude\logs\mcp-server-admindesk.log`.

## Configuration serveur (rappel)

Dans `src/main/resources/application.properties` :

```properties
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.name=admindesk-mcp
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.type=SYNC
```
