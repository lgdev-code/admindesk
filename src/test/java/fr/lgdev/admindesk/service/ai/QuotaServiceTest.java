package fr.lgdev.admindesk.service.ai;

import fr.lgdev.admindesk.repository.AiUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotaServiceTest {

    @Mock
    private AiUsageRepository repo;

    @InjectMocks
    private QuotaService quotaService;

    @Test
    void autorise_quand_sous_la_limite() {
        // 19 appels déjà faits aujourd'hui : le 20e passe
        when(repo.countByAgentIdAndDateUsageBetween(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(19L);
        assertThatCode(() -> quotaService.check(1L)).doesNotThrowAnyException();
    }

    @Test
    void refuse_au_21e_appel() {
        // 20 appels déjà faits : le 21e est refusé
        when(repo.countByAgentIdAndDateUsageBetween(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(20L);
        assertThatThrownBy(() -> quotaService.check(1L))
                .isInstanceOf(QuotaExceededException.class)
                .hasMessageContaining("Quota journalier atteint");
    }

    @Test
    void agent_null_jamais_bloque() {
        assertThatCode(() -> quotaService.check(null)).doesNotThrowAnyException();
    }
}
