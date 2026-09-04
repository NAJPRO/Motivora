package com.audin.motivora.controller.Client;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.service.AccountService;
import com.audin.motivora.storage.FileStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("me/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private static final String FOLDER = "avatars";

    private final FileStorageService fileStorageService;
    private final AccountService accountService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTOResponse> upload(@RequestParam("file") MultipartFile file) {
        String url = this.fileStorageService.storeImage(file, FOLDER);
        return ResponseEntity.ok(this.accountService.updateAvatar(url));
    }
}
