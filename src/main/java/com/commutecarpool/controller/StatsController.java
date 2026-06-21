package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.stats.StatsOverview;
import com.commutecarpool.dto.stats.StatsTrend;
import com.commutecarpool.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public ApiResponse<StatsOverview> overview() {
        return ApiResponse.success(statsService.getOverview());
    }

    @GetMapping("/trend")
    public ApiResponse<List<StatsTrend>> trend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(statsService.getTrend(startDate, endDate));
    }
}
