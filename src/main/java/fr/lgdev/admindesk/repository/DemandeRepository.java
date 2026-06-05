package fr.lgdev.admindesk.repository;

import fr.lgdev.admindesk.domain.Demande;
import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {

    Optional<Demande> findByReference(String reference);

    List<Demande> findByStatutOrderByDateCreationDesc(StatutDemande statut);
    List<Demande> findByTypeOrderByDateCreationDesc(TypeDemande type);
    List<Demande> findByPrioriteOrderByDateCreationDesc(Priorite priorite);
    List<Demande> findByAgentTraitantOrderByDateCreationDesc(String agentTraitant);

    @Query("""
            SELECT d FROM Demande d
            WHERE (:statut   IS NULL OR d.statut   = :statut)
              AND (:type     IS NULL OR d.type     = :type)
              AND (:priorite IS NULL OR d.priorite = :priorite)
              AND (:search   IS NULL
                   OR LOWER(d.nomDemandeur) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(d.description)  LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(d.reference)    LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            ORDER BY d.dateCreation DESC
            """)
    Page<Demande> rechercher(
            @Param("statut")   StatutDemande statut,
            @Param("type")     TypeDemande type,
            @Param("priorite") Priorite priorite,
            @Param("search")   String search,
            Pageable pageable
    );

    long countByStatut(StatutDemande statut);
    long countByPriorite(Priorite priorite);
    long countByType(TypeDemande type);

    @Query("SELECT COUNT(d) FROM Demande d WHERE d.agentTraitant = :agent AND d.statut = :statut")
    long countByAgentAndStatut(@Param("agent") String agent, @Param("statut") StatutDemande statut);
}
