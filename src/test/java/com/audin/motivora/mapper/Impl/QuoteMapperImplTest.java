package com.audin.motivora.mapper.Impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.audin.motivora.dto.response.QuoteResponse;
import com.audin.motivora.entity.Author;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.Role;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.QuoteStatus;
import com.audin.motivora.enums.UserRole;

class QuoteMapperImplTest {

    private final QuoteMapperImpl mapper =
            new QuoteMapperImpl(new AuthorMapperImpl(), new ThemeMapperImpl(), new AuthMapperImpl());

    private Theme theme() {
        Theme t = new Theme();
        t.setId(2);
        t.setName("Wisdom");
        t.setSlug("wisdom");
        t.setActive(true);
        return t;
    }

    @Test
    void mapsAllNestedObjectsWithoutRecursion() {
        Author author = new Author();
        author.setId(1);
        author.setName("Plato");
        author.setSlug("plato");

        Role role = new Role();
        role.setName(UserRole.USER);
        User user = new User();
        user.setId(3);
        user.setEmail("bob@example.com");
        user.setPseudo("Bob");
        user.setRole(role);

        Quote quote = new Quote();
        quote.setId(5);
        quote.setSlug("life-is");
        quote.setContent("Life is good");
        quote.setAuthor(author);
        quote.setTheme(theme());
        quote.setCreatedByUser(user);
        quote.setStatus(QuoteStatus.PUBLISHED);

        QuoteResponse response = mapper.toAdminResponse(quote);

        assertThat(response.id()).isEqualTo(5);
        assertThat(response.content()).isEqualTo("Life is good");
        assertThat(response.status()).isEqualTo("PUBLISHED");
        assertThat(response.author()).isNotNull();
        assertThat(response.author().name()).isEqualTo("Plato");
        assertThat(response.theme()).isNotNull();
        assertThat(response.theme().name()).isEqualTo("Wisdom");
        assertThat(response.createdByUser()).isNotNull();
        assertThat(response.createdByUser().role()).isEqualTo("USER");
    }

    /**
     * The public catalogue is reachable anonymously: it must never carry the submitter's
     * email address.
     */
    @Test
    void publicResponseHidesTheSubmitter() {
        Role role = new Role();
        role.setName(UserRole.USER);
        User user = new User();
        user.setId(3);
        user.setEmail("bob@example.com");
        user.setRole(role);

        Quote quote = new Quote();
        quote.setId(5);
        quote.setContent("Life is good");
        quote.setTheme(theme());
        quote.setCreatedByUser(user);
        quote.setStatus(QuoteStatus.PUBLISHED);

        QuoteResponse response = mapper.toResponse(quote);

        assertThat(response.createdByUser()).isNull();
        assertThat(response.isFavorite()).isFalse();
    }

    @Test
    void favoriteFlagIsCarriedThrough() {
        Quote quote = new Quote();
        quote.setId(5);
        quote.setContent("Life is good");
        quote.setTheme(theme());
        quote.setStatus(QuoteStatus.PUBLISHED);

        assertThat(mapper.toResponse(quote, true).isFavorite()).isTrue();
    }

    @Test
    void handlesNullAuthorAndCreator() {
        Quote quote = new Quote();
        quote.setId(6);
        quote.setContent("Anonymous");
        quote.setTheme(theme());
        quote.setStatus(QuoteStatus.PUBLISHED);

        QuoteResponse response = mapper.toResponse(quote);

        assertThat(response.author()).isNull();
        assertThat(response.createdByUser()).isNull();
        assertThat(response.theme()).isNotNull();
    }

    @Test
    void returnsNullForNullEntity() {
        assertThat(mapper.toResponse((Quote) null)).isNull();
    }
}
