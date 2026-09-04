package com.audin.motivora.controller.Admin;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.common.MessageResponse;
import com.audin.motivora.dto.response.common.PageResponse;
import com.audin.motivora.dto.request.AdminCreateUserRequest;
import com.audin.motivora.dto.request.UpdateUserRoleRequest;
import com.audin.motivora.dto.request.UpdateUserStatusRequest;
import com.audin.motivora.dto.response.UserDTOResponse;
import com.audin.motivora.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<PageResponse<UserDTOResponse>> index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(PageResponse.from(this.userService.getAll(page, size, search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTOResponse> show(@PathVariable Integer id) {
        return ResponseEntity.ok(this.userService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UserDTOResponse> create(@RequestBody @Valid AdminCreateUserRequest dto) {
        return new ResponseEntity<>(this.userService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDTOResponse> updateRole(
            @PathVariable Integer id,
            @RequestBody @Valid UpdateUserRoleRequest dto) {
        return ResponseEntity.ok(this.userService.updateRole(id, dto.getRole()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserDTOResponse> updateStatus(
            @PathVariable Integer id,
            @RequestBody @Valid UpdateUserStatusRequest dto) {
        return ResponseEntity.ok(this.userService.updateStatus(id, dto.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Integer id) {
        this.userService.delete(id);
        return ResponseEntity.ok(MessageResponse.of("User deleted successfully"));
    }
}
