package com.audin.motivora.mapper.Impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.audin.motivora.dto.response.FavoriteResponse;
import com.audin.motivora.entity.Favorite;
import com.audin.motivora.entity.Quote;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.entity.User;
import com.audin.motivora.enums.QuoteStatus;
import com.audin.motivora.mapper.QuoteMapper;

class FavoriteMapperImplTest {

    private final QuoteMapper quoteMapper =
            new QuoteMapperImpl(new AuthorMapperImpl(), new ThemeMapperImpl(), new AuthMapperImpl());
    private final FavoriteMapperImpl mapper = new FavoriteMapperImpl(quoteMapper);

    @Test
    void mapsUserIdAndNestedQuote() {
        Theme theme = new Theme();
        theme.setId(2);
        theme.setName("Wisdom");
        theme.setActive(true);

        Quote quote = new Quote();
        quote.setId(7);
        quote.setContent("Be kind");
        quote.setTheme(theme);
        quote.setStatus(QuoteStatus.PUBLISHED);

        User user = new User();
        user.setId(5);

        Favorite favorite = Favorite.builder().id(9).user(user).quote(quote).build();

        FavoriteResponse response = mapper.toDto(favorite);

        assertThat(response.id()).isEqualTo(9);
        assertThat(response.userId()).isEqualTo(5);
        assertThat(response.quote()).isNotNull();
        assertThat(response.quote().id()).isEqualTo(7);
    }
}
