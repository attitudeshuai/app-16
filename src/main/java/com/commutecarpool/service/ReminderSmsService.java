package com.commutecarpool.service;

import com.commutecarpool.entity.Carpool;
import com.commutecarpool.entity.CarpoolBooking;
import com.commutecarpool.entity.Route;
import com.commutecarpool.repository.CarpoolBookingRepository;
import com.commutecarpool.repository.CarpoolRepository;
import com.commutecarpool.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderSmsService {

    private final CarpoolBookingRepository carpoolBookingRepository;
    private final CarpoolRepository carpoolRepository;
    private final RouteRepository routeRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendTripReminderSms() {
        List<CarpoolBooking> bookings = carpoolBookingRepository.findBookingsPendingReminder();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursLater = now.plusHours(2);

        for (CarpoolBooking booking : bookings) {
            try {
                Carpool carpool = carpoolRepository.findById(booking.getCarpoolId()).orElse(null);
                if (carpool == null) {
                    continue;
                }
                Route route = routeRepository.findById(carpool.getRouteId()).orElse(null);
                if (route == null) {
                    continue;
                }
                LocalDateTime tripDateTime = parseTripDateTime(carpool.getTripDate(), route.getStartTime());
                if (tripDateTime == null) {
                    continue;
                }
                if (!tripDateTime.isBefore(twoHoursLater) && !tripDateTime.isAfter(twoHoursLater)) {
                }
                if (tripDateTime.isAfter(now) && !tripDateTime.isAfter(twoHoursLater)) {
                    if (booking.getEmergencyContactPhone() != null && booking.getEmergencyContactName() != null) {
                        doSendSms(
                                booking.getEmergencyContactPhone(),
                                booking.getEmergencyContactName(),
                                booking.getEmergencyContactRelationship(),
                                route.getStartLocation(),
                                route.getEndLocation(),
                                tripDateTime
                        );
                        booking.setReminderSmsSent(true);
                        carpoolBookingRepository.save(booking);
                        log.info("已向紧急联系人 {}({}) 发送行程提醒短信，预订ID: {}",
                                booking.getEmergencyContactName(),
                                booking.getEmergencyContactPhone(),
                                booking.getId());
                    }
                }
            } catch (Exception e) {
                log.error("发送行程提醒短信失败，预订ID: {}", booking.getId(), e);
            }
        }
    }

    private LocalDateTime parseTripDateTime(LocalDate tripDate, String startTimeStr) {
        if (tripDate == null || startTimeStr == null || startTimeStr.isEmpty()) {
            return null;
        }
        try {
            LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(tripDate, startTime);
        } catch (Exception e) {
            log.warn("解析行程时间失败，日期: {}, 时间: {}", tripDate, startTimeStr);
            return null;
        }
    }

    private void doSendSms(String phone, String contactName, String relationship,
                           String startLocation, String endLocation, LocalDateTime tripDateTime) {
        String message = String.format(
                "【通勤拼车】尊敬的%s（乘客%s），您的家人/朋友将于%s从%s前往%s，请留意其行程安全。",
                contactName, relationship != null ? relationship : "的紧急联系人",
                tripDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                startLocation, endLocation
        );
        log.info("[模拟短信发送] 手机号: {}, 内容: {}", phone, message);
    }
}
