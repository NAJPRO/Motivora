package com.audin.motivora.dto.request;

import com.audin.motivora.dto.Annotation.ExistField;
import com.audin.motivora.entity.Quote;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {
    @NotNull(message = "Quote id is required")
    @Positive(message = "Quote id must be a valid positive number")
    @ExistField(entity = Quote.class, fieldName = "id", message = "Quote's id doesn't exist")
    private Integer quoteId;
}
