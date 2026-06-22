package com.commutecarpool.service;

import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.booking.BookingRequest;
import com.commutecarpool.dto.booking.BookingResponse;
import com.commutecarpool.dto.booking.BookingStatusRequest;
import com.commutecarpool.dto.emergencycontact.EmergencyContactResponse;
import com.commutecarpool.entity.BookingStatus;
import com.commutecarpool.entity.Carpool;
import com.commutecarpool.entity.CarpoolBooking;
import com.commutecarpool.entity.CarpoolStatus;
import com.commutecarpool.entity.EmergencyContact;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.CarpoolBookingRepository;
import com.commutecarpool.repository.CarpoolRepository;
import com.commutecarpool.repository.EmergencyContactRepository;
import com.commutecarpool.security.SecurityUtils;
import com.commutecarpool.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final CarpoolBookingRepository carpoolBookingRepository;
    private final CarpoolRepository carpoolRepository;
    private final EmergencyContactRepository emergencyContactRepository;

    public PageResponse<BookingResponse> listBookings(Long carpoolId, Long passengerId, BookingStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<CarpoolBooking> bookingPage;
        if (carpoolId != null && passengerId != null && status != null) {
            bookingPage = carpoolBookingRepository.findByCarpoolIdAndPassengerIdAndStatus(carpoolId, passengerId, status, pageable);
        } else if (carpoolId != null && passengerId != null) {
            bookingPage = carpoolBookingRepository.findByCarpoolIdAndPassengerId(carpoolId, passengerId, pageable);
        } else if (carpoolId != null && status != null) {
            bookingPage = carpoolBookingRepository.findByCarpoolIdAndStatus(carpoolId, status, pageable);
        } else if (passengerId != null && status != null) {
            bookingPage = carpoolBookingRepository.findByPassengerIdAndStatus(passengerId, status, pageable);
        } else if (carpoolId != null) {
            bookingPage = carpoolBookingRepository.findByCarpoolId(carpoolId, pageable);
        } else if (passengerId != null) {
            bookingPage = carpoolBookingRepository.findByPassengerId(passengerId, pageable);
        } else if (status != null) {
            bookingPage = carpoolBookingRepository.findByStatus(status, pageable);
        } else {
            bookingPage = carpoolBookingRepository.findAll(pageable);
        }
        List<BookingResponse> content = bookingPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageUtils.toPageResponse(bookingPage, content);
    }

    public BookingResponse createBooking(BookingRequest req) {
        Carpool carpool = carpoolRepository.findById(req.getCarpoolId())
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        if (carpool.getStatus() != CarpoolStatus.OPEN) {
            throw new BusinessException(400, "拼车不可预订");
        }
        if (carpool.getAvailableSeats() < req.getSeatsBooked()) {
            throw new BusinessException(400, "座位不足");
        }
        Long passengerId = SecurityUtils.getCurrentUserId();
        List<EmergencyContact> contacts = emergencyContactRepository.findByPassengerId(passengerId);
        if (contacts.isEmpty()) {
            throw new BusinessException(400, "首次提交预约前请先添加至少一位紧急联系人");
        }
        EmergencyContact primaryContact = contacts.get(0);
        CarpoolBooking booking = new CarpoolBooking();
        booking.setCarpoolId(req.getCarpoolId());
        booking.setPassengerId(passengerId);
        booking.setSeatsBooked(req.getSeatsBooked());
        booking.setEmergencyContactName(primaryContact.getContactName());
        booking.setEmergencyContactPhone(primaryContact.getContactPhone());
        booking.setEmergencyContactRelationship(primaryContact.getRelationship());
        carpoolBookingRepository.save(booking);
        return toResponse(booking);
    }

    public BookingResponse getBooking(Long id) {
        CarpoolBooking booking = carpoolBookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "预订不存在"));
        return toResponse(booking);
    }

    public BookingResponse updateBooking(Long id, BookingRequest req) {
        CarpoolBooking booking = carpoolBookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "预订不存在"));
        if (!booking.getPassengerId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权操作");
        }
        BeanUtils.copyProperties(req, booking);
        booking.setId(id);
        carpoolBookingRepository.save(booking);
        return toResponse(booking);
    }

    public void deleteBooking(Long id) {
        CarpoolBooking booking = carpoolBookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "预订不存在"));
        if (!booking.getPassengerId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权操作");
        }
        carpoolBookingRepository.delete(booking);
    }

    @Transactional
    public BookingResponse updateBookingStatus(Long id, BookingStatusRequest req) {
        CarpoolBooking booking = carpoolBookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "预订不存在"));
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Carpool carpool = carpoolRepository.findById(booking.getCarpoolId())
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        if (!booking.getPassengerId().equals(currentUserId) && !carpool.getDriverId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作");
        }
        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(req.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的状态");
        }
        BookingStatus oldStatus = booking.getStatus();
        if (newStatus == BookingStatus.CONFIRMED) {
            carpool.setAvailableSeats(carpool.getAvailableSeats() - booking.getSeatsBooked());
            if (carpool.getAvailableSeats() <= 0) {
                carpool.setStatus(CarpoolStatus.FULL);
            }
            carpoolRepository.save(carpool);
        } else if (newStatus == BookingStatus.CANCELLED && oldStatus == BookingStatus.CONFIRMED) {
            carpool.setAvailableSeats(carpool.getAvailableSeats() + booking.getSeatsBooked());
            carpool.setStatus(CarpoolStatus.OPEN);
            carpoolRepository.save(carpool);
        }
        booking.setStatus(newStatus);
        carpoolBookingRepository.save(booking);
        return toResponse(booking);
    }

    public EmergencyContactResponse getPassengerEmergencyContactForDriver(Long bookingId) {
        Long driverId = SecurityUtils.getCurrentUserId();
        CarpoolBooking booking = carpoolBookingRepository.findByIdAndDriverId(bookingId, driverId);
        if (booking == null) {
            throw new BusinessException(404, "预订不存在或您无权查看");
        }
        if (booking.getEmergencyContactName() == null || booking.getEmergencyContactPhone() == null) {
            throw new BusinessException(404, "该乘客未设置紧急联系人信息");
        }
        EmergencyContactResponse response = new EmergencyContactResponse();
        response.setContactName(booking.getEmergencyContactName());
        response.setContactPhone(booking.getEmergencyContactPhone());
        response.setRelationship(booking.getEmergencyContactRelationship());
        return response;
    }

    private BookingResponse toResponse(CarpoolBooking booking) {
        BookingResponse response = new BookingResponse();
        BeanUtils.copyProperties(booking, response);
        response.setStatus(booking.getStatus().name());
        return response;
    }
}
