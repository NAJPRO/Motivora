package com.audin.motivora.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.audin.motivora.dto.request.AuthorRequest;
import com.audin.motivora.dto.response.AuthorResponse;
import com.audin.motivora.entity.Author;
import com.audin.motivora.mapper.AuthorMapper;
import com.audin.motivora.repository.AuthorRepository;
import com.audin.motivora.service.AuthorService;
import com.audin.motivora.service.QuoteService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorMapper authorMapper;
    private final AuthorRepository authorRepository;
    private final QuoteService quoteService;

    @Override
    public AuthorResponse create(String idOrSlug) {
        Author author = this.findAuthorByIdOrSlug(idOrSlug);
        return authorMapper.toResponse(author);
    }

    @Override
    @Transactional
    public void disable(String idOrSlug) {
        Author author = this.findAuthorByIdOrSlug(idOrSlug);
        if (!author.isActive()) {
            throw new IllegalStateException("Author is already disabled");
        }
        author.setActive(false);
        authorRepository.save(author);

        this.quoteService.disableByAuthor(author);
    }

    @Override
    @Transactional
    public void enable(String idOrSlug) {
        Author author = this.findAuthorByIdOrSlug(idOrSlug);
        if (author.isActive()) {
            throw new IllegalStateException("Author is already enabled");
        }
        author.setActive(true);
        authorRepository.save(author);

        this.quoteService.enableByAuthor(author);
    }

    @Override
    public List<AuthorResponse> getAll() {
        return authorMapper.toResponse(authorRepository.findAll());
    }

    @Override
    public AuthorResponse save(AuthorRequest dto) {
        // Gérer la sauvegarde de l'image de l'auteur plus tard
        Author author = authorMapper.toEntity(dto);
        author = authorRepository.save(author);
        return authorMapper.toResponse(author);
    }

    @Override
    public AuthorResponse update(String idOrSlug, AuthorRequest dto) {
        Author author = this.findAuthorByIdOrSlug(idOrSlug);
        author = authorMapper.updateEntity(author, dto);
        author = authorRepository.save(author);
        return authorMapper.toResponse(author);
    }

    private Author findAuthorByIdOrSlug(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            return authorRepository.findById(Integer.parseInt(idOrSlug))
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));
        } else {
            return authorRepository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));
        }
    }

}
