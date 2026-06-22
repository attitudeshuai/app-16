package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.pricing.PricingConfigRequest;
import com.commutecarpool.dto.pricing.PricingConfigResponse;
import com.commutecarpool.dto.pricing.PricingTemporaryRuleRequest;
import com.commutecarpool.dto.pricing.PricingTemporaryRuleResponse;
import com.commutecarpool.dto.pricing.SuggestedPriceResponse;
import com.commutecarpool.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pricing")
@RequiredArgsConstructor
public class AdminPricingController {

    private final PricingService pricingService;

    @GetMapping("/config")
    public ApiResponse<PricingConfigResponse> getConfig() {
        return ApiResponse.success(pricingService.getConfig());
    }

    @PutMapping("/config")
    public ApiResponse<PricingConfigResponse> updateConfig(@RequestBody @Valid PricingConfigRequest request) {
        return ApiResponse.success(pricingService.updateConfig(request));
    }

    @GetMapping("/rules")
    public ApiResponse<List<PricingTemporaryRuleResponse>> listRules() {
        return ApiResponse.success(pricingService.listRules());
    }

    @PostMapping("/rules")
    public ApiResponse<PricingTemporaryRuleResponse> createRule(@RequestBody @Valid PricingTemporaryRuleRequest request) {
        return ApiResponse.success(pricingService.createRule(request));
    }

    @PutMapping("/rules/{id}")
    public ApiResponse<PricingTemporaryRuleResponse> updateRule(
            @PathVariable Long id,
            @RequestBody @Valid PricingTemporaryRuleRequest request) {
        return ApiResponse.success(pricingService.updateRule(id, request));
    }

    @DeleteMapping("/rules/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        pricingService.deleteRule(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/simulate")
    public ApiResponse<SuggestedPriceResponse> simulatePrice(
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tripDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime tripTime,
            @RequestParam(defaultValue = "3") Integer availableSeats) {
        return ApiResponse.success(pricingService.calculateSuggestedPrice(routeId, tripDate, tripTime, availableSeats));
    }
}
