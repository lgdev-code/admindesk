package fr.lgdev.admindesk.service.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptGuardTest {

    private final PromptGuard guard = new PromptGuard();

    @Test
    void accepte_une_saisie_normale() {
        String txt = "Demande de place en crèche pour mon enfant né en mars.";
        assertThat(guard.check(txt)).isEqualTo(txt);
    }

    @Test
    void rejette_une_saisie_trop_longue() {
        String huge = "a".repeat(5000);
        assertThatThrownBy(() -> guard.check(huge))
                .isInstanceOf(InputTooLargeException.class);
    }

    @Test
    void conserve_le_null() {
        assertThat(guard.check(null)).isNull();
    }
}
