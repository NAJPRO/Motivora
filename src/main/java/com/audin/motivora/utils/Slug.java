package com.audin.motivora.utils;

public class Slug {
    public static String toSlug(String input) {
        if (input == null)
            return null;
        return input.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
