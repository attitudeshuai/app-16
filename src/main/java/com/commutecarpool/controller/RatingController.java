package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.rating.RatingRequest;
import com.commutecarpool.dto.rating.RatingResponse;
import com.commutecarpool.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carpoolratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @GetMapping
    public ApiResponse<PageResponse<RatingResponse>> list(
            @RequestParam(required = false) Long carpoolId,
            @RequestParam(required = false) Long reviewerId,
            @RequestParam(required = false) Long revieweeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(ratingService.listRatings(carpoolId, reviewerId, revieweeId, page, size));
    }

    @PostMapping
    public ApiResponse<RatingResponse> create(@RequestBody @Valid RatingRequest request) {
        return ApiResponse.success(ratingService.createRating(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RatingResponse> get(@PathVariable Long id) {
        return ApiResponse.success(ratingService.getRating(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<RatingResponse> update(@PathVariable Long id, @RequestBody @Valid RatingRequest request) {
        return ApiResponse.success(ratingService.updateRating(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ratingService.deleteRating(id);
        return ApiResponse.success("删除成功", null);
    }
}
