-- ============================================================================
--  Observabilité RAG (D5) — SCHÉMA SEUL (DDL idempotent).
--
--  Exécuté AUTOMATIQUEMENT au démarrage du profil `prod` (cf.
--  application-prod.properties : spring.sql.init.schema-locations).
--  CREATE TABLE IF NOT EXISTS => rejouable sans risque à chaque démarrage.
--
--  Les DONNÉES de démo (et le TRUNCATE destructeur) restent dans
--  db/rag-observability.sql, à lancer À LA MAIN quand on veut peupler le
--  dashboard avec un jeu fictif.
--
--  Tables créées dans le schéma par défaut du compte stagiaire (search_path),
--  à côté de vector_store.
-- ============================================================================

CREATE TABLE IF NOT EXISTS rag_audit (
    id                bigserial PRIMARY KEY,
    request_id        text,
    agent_id          bigint,
    question          text,
    sources           text,            -- sources citées, séparées par des virgules
    answer            text,
    prompt_tokens     int,
    completion_tokens int,
    retrieval_ms      int,
    total_ms          int,
    retrieved_count   int,             -- nb de documents remontés (0 = miss)
    refused           boolean,         -- réponse « refus poli »
    created_at        timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rag_feedback (
    id          bigserial PRIMARY KEY,
    request_id  text,
    vote        varchar(4),            -- 'UP' / 'DOWN'
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rag_eval_run (
    id          bigserial PRIMARY KEY,
    recall_at_3 numeric,
    mrr         numeric,
    run_at      timestamptz NOT NULL DEFAULT now()
);
