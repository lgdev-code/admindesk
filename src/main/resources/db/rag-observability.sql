-- ============================================================================
--  Observabilité RAG (D5) — tables + jeu de DÉMO pour alimenter le dashboard.
--  À exécuter UNE fois dans DataGrip / psql (connecté avec votre compte STAGIAIRE :
--  les tables sont créées dans votre schéma par défaut, comme vector_store).
--
--  En production, ces tables sont alimentées par le RagService (audit, cf. slide 23)
--  et par le bouton 👍/👎 du frontend (feedback). Ici on insère des données fictives
--  pour que le dashboard « RAG Quality » s'affiche immédiatement.
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

-- --- Données de démonstration -------------------------------------------------
TRUNCATE rag_audit, rag_feedback, rag_eval_run;

-- 86 requêtes, distribution ~ slide 21 (38/21/17/8/2 %). On utilise de VRAIS codes du corpus
-- pour que le tableau « indexé mais jamais ramené » (cross-check vector_store) soit cohérent.
INSERT INTO rag_audit (request_id, agent_id, question, sources, answer,
                       prompt_tokens, completion_tokens, retrieval_ms, total_ms,
                       retrieved_count, refused, created_at)
SELECT gen_random_uuid()::text,
       1 + (rn % 5),
       'question de démonstration #' || rn,
       src,
       'réponse de démonstration',
       300 + (rn % 60),
       70 + (rn % 40),
       600  + ((rn * 53) % 900),                 -- retrieval_ms : p95 ~ 1,4 s
       1300 + ((rn * 91) % 1600),                -- total_ms     : p95 ~ 2,8 s
       CASE WHEN rn % 45 = 0 THEN 0 ELSE 3 END,  -- ~2 % de misses
       (rn % 12 = 0),                            -- ~8 % de refus poli
       now() - make_interval(days => (rn % 30))
FROM (
    SELECT src, row_number() OVER () AS rn FROM (
        SELECT 'CUNC-PS-PC'::text       AS src FROM generate_series(1, 38)
        UNION ALL SELECT 'CCNC-COMMERCE-DETAIL' FROM generate_series(1, 21)
        UNION ALL SELECT 'CAFAT-RUAMM'          FROM generate_series(1, 17)
        UNION ALL SELECT 'ETAT-CIVIL-NAISS'     FROM generate_series(1, 8)
        UNION ALL SELECT 'COMMERCE-SOLDES'      FROM generate_series(1, 2)
    ) s
) demo;

-- 100 votes : 87 👍 / 13 👎
INSERT INTO rag_feedback (request_id, vote, created_at)
SELECT gen_random_uuid()::text,
       CASE WHEN g <= 87 THEN 'UP' ELSE 'DOWN' END,
       now() - make_interval(days => (g % 30))
FROM generate_series(1, 100) AS g;

-- dernier run d'éval (recall@3 / MRR), cf. TP12
INSERT INTO rag_eval_run (recall_at_3, mrr, run_at) VALUES (0.91, 0.86, now());
