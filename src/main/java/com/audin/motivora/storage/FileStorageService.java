package com.audin.motivora.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Stores user-supplied images. Implemented locally today; the interface is what an S3 /
 * Cloudinary backend would replace without touching the callers.
 */
public interface FileStorageService {

    /**
     * Validates and stores an image.
     *
     * @param folder logical bucket ("avatars", "authors", "themes")
     * @return the public URL to expose in API responses
     */
    String storeImage(MultipartFile file, String folder);

    /** Removes a previously stored file. Silent when it is already gone. */
    void delete(String publicUrl);
}
