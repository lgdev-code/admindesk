-- ============================================================================
--  Index GIN — accélère la recherche plein texte (lexicale) du TP12.
--  À exécuter UNE fois dans DataGrip / psql.
--  Remplacez VOTRE_SCHEMA par votre compte STAGIAIRE.
--
--  Important : l'expression de l'index doit être IDENTIQUE à celle de la
--  requête (to_tsvector('french', content)) pour que PostgreSQL l'utilise.
--  Sans cet index, ts_rank/to_tsvector sont recalculés ligne par ligne (seq scan).
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_vector_store_content_fts
    ON "VOTRE_SCHEMA".vector_store
    USING gin (to_tsvector('french', content));
