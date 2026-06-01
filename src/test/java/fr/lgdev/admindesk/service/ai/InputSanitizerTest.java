package fr.lgdev.admindesk.service.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputSanitizerTest {

    private final InputSanitizer sanitizer = new InputSanitizer();

    @Test
    void masque_le_NIR() {
        String out = sanitizer.sanitize("Mon NIR 1 75 03 75 056 042 35 voila");
        assertThat(out).contains("[NIR]").doesNotContain("042");
    }

    @Test
    void masque_l_IBAN() {
        String out = sanitizer.sanitize("IBAN FR76 1234 5678 9012 3456 7890 123 fin");
        assertThat(out).contains("[IBAN]").doesNotContain("FR76 1234");
    }

    @Test
    void masque_le_telephone() {
        assertThat(sanitizer.sanitize("Appelez le 06 12 34 56 78")).contains("[TEL]");
        assertThat(sanitizer.sanitize("tel NC 26 12 34")).contains("[TEL]");
    }

    @Test
    void masque_tout_combine() {
        String in = "M. Dupont NIR 1 75 03 75 056 042 35, tel 26 12 34, "
                + "IBAN FR76 1234 5678 9012 3456 7890 123";
        String out = sanitizer.sanitize(in);
        assertThat(out).contains("[NIR]").contains("[TEL]").contains("[IBAN]");
    }

    @Test
    void est_idempotent() {
        String in = "NIR 1 75 03 75 056 042 35 et IBAN FR76 1234 5678 9012 3456 7890 123";
        String once = sanitizer.sanitize(in);
        String twice = sanitizer.sanitize(once);
        assertThat(twice).isEqualTo(once);
    }
}
