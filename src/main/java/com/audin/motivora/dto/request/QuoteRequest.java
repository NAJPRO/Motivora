package com.audin.motivora.dto.request;

import com.audin.motivora.dto.Annotation.ExistField;
import com.audin.motivora.entity.Author;
import com.audin.motivora.entity.Theme;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuoteRequest {
    @NotBlank(message = "Quote content is required")
    @Size(min = 5, max = 500, message = "Quote must be between 5 and 500 characters")
    private String content;

    @NotNull(message = "Author is required")
    @Positive(message = "Author id must be a valid positive number")
    @ExistField(entity = Author.class, fieldName = "id", message = "Author's id doesn't exist")
    private Integer authorId;

    @NotNull(message = "Theme is required")
    @Positive(message = "Theme id must be a valid positive number")
    @ExistField(entity = Theme.class, fieldName = "id", message = "Theme's id doesn't exist")
    private Integer themeId;
}
