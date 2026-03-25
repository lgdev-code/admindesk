package fr.lgdev.admindesk.service;

import fr.lgdev.admindesk.domain.Demande;
import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import fr.lgdev.admindesk.dto.DemandeFormDTO;
import fr.lgdev.admindesk.repository.DemandeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemandeServiceTest {

    @Mock
    private DemandeRepository repo;

    @InjectMocks
    private DemandeService service;

    private DemandeFormDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new DemandeFormDTO();
        validDto.setNomDemandeur("Jean Dupont");
        validDto.setEmailDemandeur("jean.dupont@test.fr");
        validDto.setType(TypeDemande.URBANISME);
        validDto.setDescription("Demande de permis de construire pour une extension de 20m².");
        validDto.setStatut(StatutDemande.EN_ATTENTE);
        validDto.setPriorite(Priorite.NORMALE);
    }

    @Test
    void create_shouldPersistAndReturnDemande() {
        var saved = new Demande();
        saved.setReference("DEM-2026-00001");
        when(repo.count()).thenReturn(0L);
        when(repo.save(any())).thenReturn(saved);

        var result = service.create(validDto);

        assertThat(result.getReference()).isEqualTo("DEM-2026-00001");
        verify(repo, times(1)).save(any(Demande.class));
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_shouldUpdateFields() {
        var existing = new Demande();
        existing.setReference("DEM-2026-00042");
        existing.setNomDemandeur("Old Name");
        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        validDto.setNomDemandeur("New Name");
        var result = service.update(1L, validDto);

        assertThat(result.getNomDemandeur()).isEqualTo("New Name");
    }

    @Test
    void delete_shouldDeleteWhenExists() {
        var existing = new Demande();
        existing.setReference("DEM-2026-00010");
        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repo, times(1)).delete(existing);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(repo.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repo, never()).delete(any());
    }
}
