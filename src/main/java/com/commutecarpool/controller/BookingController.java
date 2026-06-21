package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.booking.BookingRequest;
import com.commutecarpool.dto.booking.BookingResponse;
import com.commutecarpool.dto.booking.BookingStatusRequest;
import com.commutecarpool.entity.BookingStatus;
import com.commutecarpool.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carpoolbookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<PageResponse<BookingResponse>> list(
            @RequestParam(required = false) Long carpoolId,
            @RequestParam(required = false) Long passengerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        BookingStatus statusEnum = status != null ? BookingStatus.valueOf(status) : null;
        return ApiResponse.success(bookingService.listBookings(carpoolId, passengerId, statusEnum, page, size));
    }

    @PostMapping
    public ApiResponse<BookingResponse> create(@RequestBody @Valid BookingRequest request) {
        return ApiResponse.success(bookingService.createBooking(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> get(@PathVariable Long id) {
        return ApiResponse.success(bookingService.getBooking(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<BookingResponse> update(@PathVariable Long id, @RequestBody @Valid BookingRequest request) {
        return ApiResponse.success(bookingService.updateBooking(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ApiResponse.success("删除成功", null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<BookingResponse> updateStatus(@PathVariable Long id, @RequestBody @Valid BookingStatusRequest request) {
        return ApiResponse.success(bookingService.updateBookingStatus(id, request));
    }
}
