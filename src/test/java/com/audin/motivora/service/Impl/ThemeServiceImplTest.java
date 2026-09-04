package com.audin.motivora.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.audin.motivora.entity.Theme;
import com.audin.motivora.mapper.ThemeMapper;
import com.audin.motivora.repository.ThemeRepository;
import com.audin.motivora.service.QuoteService;

@ExtendWith(MockitoExtension.class)
class ThemeServiceImplTest {

    @Mock ThemeMapper themeMapper;
    @Mock ThemeRepository themeRepository;
    @Mock QuoteService quoteService;

    @InjectMocks ThemeServiceImpl service;

    @Test
    void disableActiveThemeDeactivatesAndCascades() {
        Theme theme = new Theme();
        theme.setActive(true);
        when(themeRepository.findById(1)).thenReturn(Optional.of(theme));

        service.disable("1");

        assertThat(theme.isActive()).isFalse();
        verify(themeRepository).save(theme);
        verify(quoteService).disableByTheme(theme);
    }

    @Test
    void disableAlreadyInactiveThemeThrowsAndDoesNotCascade() {
        Theme theme = new Theme();
        theme.setActive(false);
        when(themeRepository.findById(1)).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> service.disable("1"))
                .isInstanceOf(IllegalStateException.class);

        verify(quoteService, never()).disableByTheme(any());
    }

    @Test
    void enableInactiveThemeActivatesAndCascades() {
        Theme theme = new Theme();
        theme.setActive(false);
        when(themeRepository.findById(1)).thenReturn(Optional.of(theme));

        service.enable("1");

        assertThat(theme.isActive()).isTrue();
        verify(quoteService).enableByTheme(theme);
    }

    @Test
    void enableAlreadyActiveThemeThrows() {
        Theme theme = new Theme();
        theme.setActive(true);
        when(themeRepository.findById(1)).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> service.enable("1"))
                .isInstanceOf(IllegalStateException.class);
    }
}
