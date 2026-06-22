package com.commutecarpool.service;

import com.commutecarpool.dto.emergencycontact.EmergencyContactRequest;
import com.commutecarpool.dto.emergencycontact.EmergencyContactResponse;
import com.commutecarpool.entity.EmergencyContact;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.EmergencyContactRepository;
import com.commutecarpool.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;

    public List<EmergencyContactResponse> listMyContacts() {
        Long passengerId = SecurityUtils.getCurrentUserId();
        List<EmergencyContact> contacts = emergencyContactRepository.findByPassengerId(passengerId);
        return contacts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EmergencyContactResponse createContact(EmergencyContactRequest req) {
        Long passengerId = SecurityUtils.getCurrentUserId();
        EmergencyContact contact = new EmergencyContact();
        BeanUtils.copyProperties(req, contact);
        contact.setPassengerId(passengerId);
        emergencyContactRepository.save(contact);
        return toResponse(contact);
    }

    public EmergencyContactResponse updateContact(Long id, EmergencyContactRequest req) {
        Long passengerId = SecurityUtils.getCurrentUserId();
        EmergencyContact contact = emergencyContactRepository.findByIdAndPassengerId(id, passengerId)
                .orElseThrow(() -> new BusinessException(404, "紧急联系人不存在"));
        BeanUtils.copyProperties(req, contact);
        emergencyContactRepository.save(contact);
        return toResponse(contact);
    }

    public void deleteContact(Long id) {
        Long passengerId = SecurityUtils.getCurrentUserId();
        EmergencyContact contact = emergencyContactRepository.findByIdAndPassengerId(id, passengerId)
                .orElseThrow(() -> new BusinessException(404, "紧急联系人不存在"));
        emergencyContactRepository.delete(contact);
    }

    public List<EmergencyContactResponse> getContactsByPassengerId(Long passengerId) {
        List<EmergencyContact> contacts = emergencyContactRepository.findByPassengerId(passengerId);
        return contacts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public boolean hasEmergencyContact(Long passengerId) {
        return emergencyContactRepository.existsByPassengerId(passengerId);
    }

    private EmergencyContactResponse toResponse(EmergencyContact contact) {
        EmergencyContactResponse response = new EmergencyContactResponse();
        BeanUtils.copyProperties(contact, response);
        return response;
    }
}
