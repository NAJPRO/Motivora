package com.audin.motivora.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.audin.motivora.entity.Author;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.QuoteStatus;
import com.audin.motivora.repository.AuthorRepository;
import com.audin.motivora.repository.QuoteRepository;
import com.audin.motivora.repository.ThemeRepository;
import com.audin.motivora.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * What the mobile app can reach before signing in, and what it must never see there.
 */
class PublicCatalogueIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private UserRepository userRepository;

    private Theme theme;
    private Quote quote;

    @BeforeEach
    void seedCatalogue() {
        long suffix = System.nanoTime();

        this.theme = new Theme();
        this.theme.setName("Motivation " + suffix);
        this.theme.setSlug("motivation-" + suffix);
        this.theme.setActive(true);
        this.theme = this.themeRepository.save(this.theme);

        Author author = new Author();
        author.setName("Sénèque " + suffix);
        author.setSlug("seneque-" + suffix);
        author.setActive(true);
        author = this.authorRepository.save(author);

        this.quote = new Quote();
        this.quote.setContent("Ce n'est pas parce que les choses sont difficiles que nous n'osons pas.");
        this.quote.setSlug("difficiles-" + suffix);
        this.quote.setAuthor(author);
        this.quote.setTheme(this.theme);
        this.quote.setStatus(QuoteStatus.PUBLISHED);
        this.quote = this.quoteRepository.save(this.quote);
    }

    @Test
    @DisplayName("quotes are browsable anonymously, in the stable page envelope")
    void quotesArePublicAndPaged() throws Exception {
        this.mockMvc.perform(get("/api/v1/quotes").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.hasNext").exists())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("the public catalogue never exposes the submitter's email")
    void publicQuotesHideTheSubmitter() throws Exception {
        User submitter = this.userRepository.findAll().stream().findFirst().orElse(null);
        if (submitter != null) {
            this.quote.setCreatedByUser(submitter);
            this.quoteRepository.save(this.quote);
        }

        this.mockMvc.perform(get("/api/v1/quotes/" + this.quote.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdByUser").doesNotExist())
                .andExpect(jsonPath("$.isFavorite").value(false));
    }

    @Test
    @DisplayName("themes and authors are browsable anonymously")
    void themesAndAuthorsArePublic() throws Exception {
        this.mockMvc.perform(get("/api/v1/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        this.mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("quotes can be filtered by theme slug")
    void quotesCanBeFilteredByTheme() throws Exception {
        this.mockMvc.perform(get("/api/v1/quotes/theme/" + this.theme.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].theme.slug").value(this.theme.getSlug()));
    }

    @Test
    @DisplayName("the quote of the day does not change between two calls")
    void quoteOfTheDayIsStable() throws Exception {
        String first = this.mockMvc.perform(get("/api/v1/quotes/daily"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String second = this.mockMvc.perform(get("/api/v1/quotes/daily"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(
                        this.objectMapper.readTree(first).get("id"))
                .isEqualTo(this.objectMapper.readTree(second).get("id"));
    }

    @Test
    @DisplayName("unversioned URLs still resolve, so the admin front keeps working")
    void legacyUnversionedPathStillWorks() throws Exception {
        this.mockMvc.perform(get("/api/quotes").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("favouriting is idempotent and surfaces on the quote itself")
    void favoritesAreIdempotentAndReflectedOnQuotes() throws Exception {
        String token = this.signUp();

        this.mockMvc.perform(put("/api/v1/favorites/" + this.quote.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        // Same call again: still favourited, unlike a toggle that would undo it.
        this.mockMvc.perform(put("/api/v1/favorites/" + this.quote.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/api/v1/quotes/" + this.quote.getSlug())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFavorite").value(true));

        this.mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/favorites/" + this.quote.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        this.mockMvc.perform(get("/api/v1/quotes/" + this.quote.getSlug())
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.isFavorite").value(false));
    }

    private String signUp() throws Exception {
        String body = this.mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", "catalogue-test-" + System.nanoTime())
                        .content(this.objectMapper.writeValueAsString(Map.of(
                                "first_name", "Cat",
                                "last_name", "Tester",
                                "email", "catalogue-" + System.nanoTime() + "@example.com",
                                "password", "Password1"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = this.objectMapper.readTree(body);
        return json.get("accessToken").asText();
    }
}
