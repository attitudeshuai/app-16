package com.commutecarpool.controller;

import com.commutecarpool.dto.ApiResponse;
import com.commutecarpool.dto.emergencycontact.EmergencyContactRequest;
import com.commutecarpool.dto.emergencycontact.EmergencyContactResponse;
import com.commutecarpool.service.EmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-contacts")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    @GetMapping
    public ApiResponse<List<EmergencyContactResponse>> listMyContacts() {
        return ApiResponse.success(emergencyContactService.listMyContacts());
    }

    @PostMapping
    public ApiResponse<EmergencyContactResponse> create(@RequestBody @Valid EmergencyContactRequest request) {
        return ApiResponse.success(emergencyContactService.createContact(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<EmergencyContactResponse> update(@PathVariable Long id, @RequestBody @Valid EmergencyContactRequest request) {
        return ApiResponse.success(emergencyContactService.updateContact(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        emergencyContactService.deleteContact(id);
        return ApiResponse.success("删除成功", null);
    }
}
