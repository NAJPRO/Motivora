package com.audin.motivora.mapper.Impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.audin.motivora.dto.request.ThemeRequest;
import com.audin.motivora.dto.response.ThemeResponse;
import com.audin.motivora.entity.Theme;

class ThemeMapperImplTest {

    private final ThemeMapperImpl mapper = new ThemeMapperImpl();

    @Test
    void toEntityCopiesFields() {
        ThemeRequest request = new ThemeRequest();
        request.setName("Motivation");
        request.setDescription("Daily boost");
        request.setColor("#FF5733");
        request.setImageUrl("http://x/y.png");

        Theme theme = mapper.toEntity(request);

        assertThat(theme.getName()).isEqualTo("Motivation");
        assertThat(theme.getDescription()).isEqualTo("Daily boost");
        assertThat(theme.getColor()).isEqualTo("#FF5733");
    }

    @Test
    void toResponseMapsFields() {
        Theme theme = new Theme();
        theme.setId(1);
        theme.setSlug("motivation");
        theme.setName("Motivation");
        theme.setActive(true);

        ThemeResponse response = mapper.toResponse(theme);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.slug()).isEqualTo("motivation");
        assertThat(response.name()).isEqualTo("Motivation");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void toResponseReturnsNullForNull() {
        assertThat(mapper.toResponse((Theme) null)).isNull();
    }

    @Test
    void toEntityUpdateOnlyChangesProvidedName() {
        Theme theme = new Theme();
        theme.setName("Old");
        theme.setColor("#000000");

        ThemeRequest request = new ThemeRequest();
        request.setName("New");
        request.setColor("#000000");

        Theme updated = mapper.toEntityUpdate(theme, request);

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getColor()).isEqualTo("#000000");
    }
}
