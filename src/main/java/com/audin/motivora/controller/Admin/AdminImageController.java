package com.audin.motivora.controller.Admin;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.audin.motivora.storage.FileStorageService;

import lombok.RequiredArgsConstructor;

/**
 * Uploads the catalogue artwork (author portraits, theme covers) and returns the URL the
 * admin front then submits in the matching {@code AuthorRequest} / {@code ThemeRequest}.
 */
@RestController
@RequestMapping("admin/images")
@RequiredArgsConstructor
public class AdminImageController {

    private final FileStorageService fileStorageService;

    @PostMapping(path = "/{folder}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @PathVariable String folder,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", this.fileStorageService.storeImage(file, folder)));
    }
}
