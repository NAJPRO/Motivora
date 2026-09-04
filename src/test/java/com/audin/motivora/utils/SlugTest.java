package com.audin.motivora.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugTest {

    @Test
    void lowercasesAndHyphenatesSpaces() {
        assertThat(Slug.toSlug("Hello World")).isEqualTo("hello-world");
    }

    @Test
    void stripsSpecialCharactersAndEdges() {
        assertThat(Slug.toSlug("  Hello, World!  ")).isEqualTo("hello-world");
    }

    @Test
    void collapsesMultipleSeparators() {
        assertThat(Slug.toSlug("Live   is...beautiful")).isEqualTo("live-is-beautiful");
    }

    @Test
    void returnsNullForNull() {
        assertThat(Slug.toSlug(null)).isNull();
    }
}
