package com.audin.motivora.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThemeRequest {
    private String name;
    private String description;
    private String color;
    private String imageUrl;
}
