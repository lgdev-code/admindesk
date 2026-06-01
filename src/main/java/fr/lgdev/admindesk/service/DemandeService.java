package fr.lgdev.admindesk.service;

import fr.lgdev.admindesk.domain.Demande;
import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import fr.lgdev.admindesk.dto.DemandeFormDTO;
import fr.lgdev.admindesk.repository.DemandeRepository;
import fr.lgdev.admindesk.service.ai.AIService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DemandeService {

    private final DemandeRepository repo;
    private final AIService         aiService;
    private final AtomicLong        seq = new AtomicLong(100);

    public Page<Demande> search(StatutDemande statut, TypeDemande type,
                                Priorite priorite, String search, int page) {
        var pageable = PageRequest.of(page, 10, Sort.by("dateCreation").descending());
        return repo.rechercher(statut, type, priorite,
                (search != null && search.isBlank()) ? null : search,
                pageable);
    }

    public Demande findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));
    }

    public Demande findByReference(String ref) {
        return repo.findByReference(ref)
                .orElseThrow(() -> new EntityNotFoundException("Reference not found: " + ref));
    }

    @Transactional
    public Demande create(DemandeFormDTO dto) {
        var d = new Demande();
        d.setReference(generateReference());
        applyDto(d, dto);
        var saved = repo.save(d);
        log.info("New request created: {}", saved.getReference());
        return saved;
    }

    @Transactional
    public Demande update(Long id, DemandeFormDTO dto) {
        var d = findById(id);
        applyDto(d, dto);
        log.info("Request updated: {}", d.getReference());
        return repo.save(d);
    }

    @Transactional
    public void delete(Long id) {
        var d = findById(id);
        log.warn("Deleting request: {}", d.getReference());
        repo.delete(d);
    }

    @Transactional
    public Demande summarize(Long id) {
        Demande demande = findById(id);
        demande.setResumeIa(aiService.summarize(demande));
        return repo.save(demande);
    }

    @Transactional
    public Demande reformulate(Long id, Long agentId) {
        Demande demande = findById(id);
        demande.setReformulationIa(aiService.reformulate(demande, agentId));
        return repo.save(demande);
    }

    public Map<String, Long> statsByStatus() {
        var map = new LinkedHashMap<String, Long>();
        for (StatutDemande s : StatutDemande.values()) {
            map.put(s.getLibelle(), repo.countByStatut(s));
        }
        return map;
    }

    public Map<String, Long> statsByType() {
        var map = new LinkedHashMap<String, Long>();
        for (TypeDemande t : TypeDemande.values()) {
            long count = repo.countByType(t);
            if (count > 0) map.put(t.getLibelle(), count);
        }
        return map;
    }

    public Map<String, Long> statsByPriority() {
        var map = new LinkedHashMap<String, Long>();
        for (Priorite p : Priorite.values()) {
            map.put(p.getLibelle(), repo.countByPriorite(p));
        }
        return map;
    }

    public long count() {
        return repo.count();
    }

    private void applyDto(Demande d, DemandeFormDTO dto) {
        d.setNomDemandeur(dto.getNomDemandeur());
        d.setEmailDemandeur(dto.getEmailDemandeur());
        d.setTelephoneDemandeur(dto.getTelephoneDemandeur());
        d.setType(dto.getType());
        d.setDescription(dto.getDescription());
        d.setStatut(dto.getStatut() != null ? dto.getStatut() : StatutDemande.EN_ATTENTE);
        d.setPriorite(dto.getPriorite() != null ? dto.getPriorite() : Priorite.NORMALE);
        d.setAgentTraitant(dto.getAgentTraitant());
        d.setCommentaireAgent(dto.getCommentaireAgent());
    }

    private String generateReference() {
        String annee = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        long num = seq.incrementAndGet() + repo.count();
        return "DEM-%s-%05d".formatted(annee, num);
    }
}
