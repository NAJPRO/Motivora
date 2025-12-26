package com.audin.motivora.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuoteRequest {
    private String content;
    private Integer authorId;
    private Integer createdByUserId;
    
}
