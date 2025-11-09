package com.audin.motivora.controller.Auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.request.ResetPasswordDTORequest;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(path = "reset-password")
@Validated
public class ResetPasswordController {
    
    @PostMapping("path")
    public ResponseEntity<Map<String, String>> sendOtpCode(@RequestBody ResetPasswordDTORequest dto) {
        
        return ResponseEntity.ok(Collections.singletonMap("message", "An OTP code has been sent to you. Verify your gmail account"));
    }
    
}
