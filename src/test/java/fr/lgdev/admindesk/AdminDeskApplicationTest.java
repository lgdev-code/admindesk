package fr.lgdev.admindesk;

import fr.lgdev.admindesk.repository.DemandeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminDeskApplicationTest {

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private DemandeRepository repo;

    @Test
    void contextLoads() {
        assertThat(repo).isNotNull();
    }

    @Test
    void dataIsLoaded() {
        assertThat(repo.count()).isGreaterThan(0);
    }

    @Test
    void demandeListPageReturns200() {
        var response = http.getForEntity("/demandes", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void apiStatsReturns200() {
        var response = http.getForEntity("/api/v1/demandes/stats", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void unknownIdReturns404() {
        var response = http.getForEntity("/api/v1/demandes/99999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
