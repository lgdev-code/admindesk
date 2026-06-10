package fr.lgdev.admindesk.dto;

/** Part d'une source dans les citations du RAG. lowUsage = indexée mais peu ramenée. */
public record SourceShare(String source, long count, double sharePct, boolean lowUsage) {
}
