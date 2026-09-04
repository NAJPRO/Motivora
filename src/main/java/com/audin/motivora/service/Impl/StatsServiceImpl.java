package com.audin.motivora.service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.audin.motivora.dto.response.AdminStatsResponse;
import com.audin.motivora.enums.QuoteStatus;
import com.audin.motivora.repository.AuthorRepository;
import com.audin.motivora.repository.QuoteRepository;
import com.audin.motivora.repository.ThemeRepository;
import com.audin.motivora.repository.UserRepository;
import com.audin.motivora.service.StatsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final QuoteRepository quoteRepository;
    private final AuthorRepository authorRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                quoteRepository.count(),
                authorRepository.count(),
                themeRepository.count(),
                userRepository.count(),
                quoteRepository.countByStatus(QuoteStatus.REVIEW_PENDING));
    }
}
