package com.audin.motivora.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.audin.motivora.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filesystem-backed image storage.
 *
 * Client-supplied filenames are never reused: the stored name is a fresh UUID with an
 * extension derived from the detected content type, which rules out path traversal and
 * double-extension tricks. The bytes are decoded once to confirm the file really is an
 * image rather than something wearing an image MIME type.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private static final Set<String> ALLOWED_FOLDERS = Set.of("avatars", "authors", "themes");

    public static final String URL_PREFIX = "/uploads/";

    private final StorageProperties properties;

    @Override
    public String storeImage(MultipartFile file, String folder) {
        if (!ALLOWED_FOLDERS.contains(folder)) {
            throw new IllegalArgumentException("Unsupported storage folder: " + folder);
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("FILE_REQUIRED", "No file was uploaded");
        }
        if (file.getSize() > this.properties.getMaxImageSize()) {
            throw new BusinessException("FILE_TOO_LARGE",
                    "The image must not exceed " + (this.properties.getMaxImageSize() / (1024 * 1024)) + " MB");
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new BusinessException("UNSUPPORTED_FILE_TYPE", "Only JPEG, PNG and WebP images are accepted");
        }

        this.assertDecodesAsImage(file);

        String storedName = UUID.randomUUID() + extension;
        Path target = this.folderPath(folder).resolve(storedName);

        try (InputStream input = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Failed to store uploaded image", ex);
            throw new BusinessException("UPLOAD_FAILED", "The image could not be stored");
        }

        return this.properties.getPublicBaseUrl() + URL_PREFIX + folder + "/" + storedName;
    }

    @Override
    public void delete(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return;
        }
        int index = publicUrl.indexOf(URL_PREFIX);
        if (index < 0) {
            // An external URL we do not own.
            return;
        }

        String relative = publicUrl.substring(index + URL_PREFIX.length());
        Path root = this.rootPath();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            log.warn("Refusing to delete a path outside the storage root");
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            log.warn("Could not delete stored file {}: {}", relative, ex.getMessage());
        }
    }

    /**
     * A MIME type is client-supplied metadata; decoding the bytes is what proves the
     * upload is an image and not a script renamed to .png.
     */
    private void assertDecodesAsImage(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            if (ImageIO.read(input) == null) {
                throw new BusinessException("INVALID_IMAGE", "The uploaded file is not a readable image");
            }
        } catch (IOException ex) {
            throw new BusinessException("INVALID_IMAGE", "The uploaded file is not a readable image");
        }
    }

    private Path folderPath(String folder) {
        return this.rootPath().resolve(folder);
    }

    private Path rootPath() {
        return Paths.get(this.properties.getLocation()).toAbsolutePath().normalize();
    }
}
