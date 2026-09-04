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
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

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
        if (!theme.isActive()) {
            throw new IllegalStateException("Le thème est déjà désactivé");
        }
        theme.setActive(false);
        this.themeRepository.save(theme);
        this.quoteService.disableByTheme(theme);
    }

    @Override
    @Transactional
    public void enable(String idOrSlug) {
        Theme theme = this.findByIdOrSlug(idOrSlug);
        if (theme.isActive()) {
            throw new IllegalStateException("Le thème est déjà activé");
        }
        theme.setActive(true);
        this.themeRepository.save(theme);
        this.quoteService.enableByTheme(theme);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThemeResponse> getAll() {
        return this.themeMapper.toResponse(this.themeRepository.findAll());
    }

    @Override
    @Transactional
    public ThemeResponse save(ThemeRequest request) {
        Theme theme = this.themeMapper.toEntity(request);
        theme = this.themeRepository.save(theme);
        return this.themeMapper.toResponse(theme);
    }

    @Override
    @Transactional
    public ThemeResponse update(String idOrSlug, ThemeRequest request) {
        Theme theme = this.findByIdOrSlug(idOrSlug);
        theme = themeMapper.toEntityUpdate(theme, request);
        this.themeRepository.saveAndFlush(theme);
        return themeMapper.toResponse(theme);
    }

    @Override
    @Transactional(readOnly = true)
    public ThemeResponse create(String idOrSlug) {
        Theme theme = this.findByIdOrSlug(idOrSlug);

        return themeMapper.toResponse(theme);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThemeResponse> getAllActive() {
        return this.themeMapper.toResponse(this.themeRepository.findByIsActiveTrueOrderByNameAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public ThemeResponse getActive(String idOrSlug) {
        Theme theme = idOrSlug.matches("\\d+")
                ? this.themeRepository.findByIdAndIsActiveTrue(Integer.parseInt(idOrSlug))
                        .orElseThrow(() -> new EntityNotFoundException("Theme not found"))
                : this.themeRepository.findBySlugAndIsActiveTrue(idOrSlug)
                        .orElseThrow(() -> new EntityNotFoundException("Theme not found"));
        return this.themeMapper.toResponse(theme);
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
