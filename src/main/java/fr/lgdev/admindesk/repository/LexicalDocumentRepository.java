package fr.lgdev.admindesk.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lgdev.admindesk.service.rag.DocumentRetriever;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Accès aux données pour la recherche LEXICALE (plein texte PostgreSQL / BM25).
 * Toute la mécanique bas niveau vit ici : SQL, mapping ResultSet -> Document.
 * Le reste de l'application n'en voit que {@link DocumentRetriever}.
 */
@Repository
@Profile("prod")
@Slf4j
public class LexicalDocumentRepository implements DocumentRetriever {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final String schema;

    public LexicalDocumentRepository(JdbcTemplate jdbcTemplate,
                                     ObjectMapper objectMapper,
                                     @Value("${spring.ai.vectorstore.pgvector.schema-name}") String schema) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.schema = schema;
    }

    @Override
    public List<Document> retrieve(String query, int topK) {
        String sql = """
                SELECT id, content, metadata,
                       ts_rank(to_tsvector('french', content), plainto_tsquery('french', ?)) AS rank
                FROM %s.vector_store
                WHERE to_tsvector('french', content) @@ plainto_tsquery('french', ?)
                ORDER BY rank DESC
                LIMIT ?
                """.formatted(schema);
        return jdbcTemplate.query(sql, this::mapDocument, query, query, topK);
    }

    /** Reconstruit un Document Spring AI depuis une ligne (metadata JSONB -> Map). */
    private Document mapDocument(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString("id");
        String content = rs.getString("content");
        Map<String, Object> metadata;
        try {
            metadata = objectMapper.readValue(rs.getString("metadata"), new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("metadata illisible pour le document {} : {}", id, e.getMessage());
            metadata = new HashMap<>();
        }
        return new Document(id, content, metadata);
    }
}
