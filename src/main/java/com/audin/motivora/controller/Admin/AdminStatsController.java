package com.audin.motivora.controller.Admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audin.motivora.dto.response.AdminStatsResponse;
import com.audin.motivora.service.StatsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<AdminStatsResponse> stats() {
        return ResponseEntity.ok(this.statsService.getStats());
    }
}
