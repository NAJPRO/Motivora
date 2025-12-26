package com.audin.motivora.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.audin.motivora.dto.request.ThemeRequest;
import com.audin.motivora.dto.response.ThemeResponse;
import com.audin.motivora.entity.Theme;
import com.audin.motivora.mapper.ThemeMapper;
import com.audin.motivora.repository.ThemeRepository;
import com.audin.motivora.service.QuoteService;
import com.audin.motivora.service.ThemeService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThemeServiceImpl implements ThemeService {
    private final ThemeMapper themeMapper;
    private final ThemeRepository themeRepository;
    private final QuoteService quoteService;

    @Override
    @Transactional
    public void disable(String idOrSlug) {
        Theme theme = this.findByIdOrSlug(idOrSlug);
        if (theme.isActive()) {
            new RuntimeException("Le thème est déjà désactivé");
        }
        theme.setActive(false);
        this.quoteService.disableByTheme(theme);
    }

    @Override
    @Transactional
    public void enable(String idOrSlug) {
        Theme theme = this.findByIdOrSlug(idOrSlug);
        if (!theme.isActive()) {
            new RuntimeException("Le thème est déjà activé");
        }
        theme.setActive(true);
        this.quoteService.enableByTheme(theme);
    }

    @Override
    public List<ThemeResponse> getAll() {
        return this.themeMapper.toResponse(this.themeRepository.findAll());
    }

    @Override
    public void save(ThemeRequest request) {
        Theme theme = this.themeMapper.toEntity(request);
        this.themeRepository.save(theme);
    }

    @Override
    public ThemeResponse update(String idOrSlug, ThemeRequest request) {
        Theme theme = this.findByIdOrSlug(idOrSlug);
        theme = themeMapper.toEntityUpdate(theme, request);
        return themeMapper.toResponse(theme);
    }

    @Override
    public ThemeResponse create(String idOrSlug) {
        Theme theme = this.findByIdOrSlug(idOrSlug);

        return themeMapper.toResponse(theme);
    }

    private Theme findByIdOrSlug(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            return themeRepository.findById(Integer.parseInt(idOrSlug))
                    .orElseThrow(() -> new EntityNotFoundException("Theme does not exist"));
        } else {
            return themeRepository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new EntityNotFoundException("Theme not found"));
        }
    }
}
