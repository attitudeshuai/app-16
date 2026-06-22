package com.commutecarpool.service;

import com.commutecarpool.dto.pricing.PricingConfigRequest;
import com.commutecarpool.dto.pricing.PricingConfigResponse;
import com.commutecarpool.dto.pricing.PricingTemporaryRuleRequest;
import com.commutecarpool.dto.pricing.PricingTemporaryRuleResponse;
import com.commutecarpool.dto.pricing.SuggestedPriceResponse;
import com.commutecarpool.entity.Carpool;
import com.commutecarpool.entity.PricingConfig;
import com.commutecarpool.entity.PricingTemporaryRule;
import com.commutecarpool.entity.Route;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.CarpoolRepository;
import com.commutecarpool.repository.PricingConfigRepository;
import com.commutecarpool.repository.PricingTemporaryRuleRepository;
import com.commutecarpool.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingService {

    private static final LocalTime MORNING_PEAK_START = LocalTime.of(7, 0);
    private static final LocalTime MORNING_PEAK_END = LocalTime.of(9, 30);
    private static final LocalTime EVENING_PEAK_START = LocalTime.of(17, 0);
    private static final LocalTime EVENING_PEAK_END = LocalTime.of(19, 30);

    private static final int HEAT_WINDOW_DAYS = 7;
    private static final BigDecimal DEFAULT_DISTANCE_KM = BigDecimal.valueOf(15);

    private final PricingConfigRepository configRepository;
    private final PricingTemporaryRuleRepository ruleRepository;
    private final RouteRepository routeRepository;
    private final CarpoolRepository carpoolRepository;

    public PricingConfig getOrCreateDefaultConfig() {
        return configRepository.findByConfigKey("DEFAULT")
                .orElseGet(() -> {
                    PricingConfig config = new PricingConfig();
                    return configRepository.save(config);
                });
    }

    public PricingConfigResponse getConfig() {
        PricingConfig config = getOrCreateDefaultConfig();
        return toConfigResponse(config);
    }

    @Transactional
    public PricingConfigResponse updateConfig(PricingConfigRequest request) {
        if (request.getMinPricePerSeat().compareTo(request.getMaxPricePerSeat()) >= 0) {
            throw new BusinessException(400, "最低价格必须小于最高价格");
        }
        PricingConfig config = getOrCreateDefaultConfig();
        BeanUtils.copyProperties(request, config);
        configRepository.save(config);
        return toConfigResponse(config);
    }

    public List<PricingTemporaryRuleResponse> listRules() {
        return ruleRepository.findAllByOrderByPriorityDescCreatedAtDesc()
                .stream()
                .map(this::toRuleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PricingTemporaryRuleResponse createRule(PricingTemporaryRuleRequest request) {
        validateRuleDates(request.getStartDate(), request.getEndDate());
        validateRuleTimes(request.getStartTime(), request.getEndTime());

        PricingTemporaryRule rule = new PricingTemporaryRule();
        BeanUtils.copyProperties(request, rule);
        ruleRepository.save(rule);
        return toRuleResponse(rule);
    }

    @Transactional
    public PricingTemporaryRuleResponse updateRule(Long id, PricingTemporaryRuleRequest request) {
        PricingTemporaryRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "临时计价规则不存在"));
        validateRuleDates(request.getStartDate(), request.getEndDate());
        validateRuleTimes(request.getStartTime(), request.getEndTime());

        BeanUtils.copyProperties(request, rule);
        ruleRepository.save(rule);
        return toRuleResponse(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new BusinessException(404, "临时计价规则不存在");
        }
        ruleRepository.deleteById(id);
    }

    public SuggestedPriceResponse calculateSuggestedPrice(Long routeId, LocalDate tripDate, LocalTime tripTime, Integer availableSeats) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new BusinessException(404, "路线不存在"));

        PricingConfig config = getOrCreateDefaultConfig();
        LocalTime effectiveTime = resolveTripTime(route, tripTime);

        BigDecimal distance = getEffectiveDistance(route);

        BigDecimal basePrice = distance
                .multiply(config.getBasePricePerKm())
                .multiply(config.getDistanceWeight())
                .setScale(2, RoundingMode.HALF_UP);

        boolean isPeakHour = isPeakHour(tripDate, effectiveTime);
        BigDecimal peakHourMultiplier = isPeakHour ? config.getPeakHourMultiplier() : config.getOffPeakMultiplier();

        LocalDate heatWindowStart = tripDate.minusDays(HEAT_WINDOW_DAYS);
        LocalDate heatWindowEnd = tripDate.plusDays(HEAT_WINDOW_DAYS);
        long activeCarpools = carpoolRepository.countActiveCarpoolsByRouteAndDateRange(routeId, heatWindowStart, heatWindowEnd);
        long recentBookings = carpoolRepository.countBookingsByRouteAndDateRange(routeId, heatWindowStart, heatWindowEnd);
        long heatIndicator = activeCarpools + recentBookings;

        BigDecimal heatMultiplier = config.getHeatMultiplierBase()
                .add(config.getHeatMultiplierPerBooking().multiply(BigDecimal.valueOf(heatIndicator)));
        if (heatMultiplier.compareTo(config.getHeatMultiplierMax()) > 0) {
            heatMultiplier = config.getHeatMultiplierMax();
        }
        heatMultiplier = heatMultiplier.setScale(4, RoundingMode.HALF_UP);

        BigDecimal seatScarcityMultiplier = BigDecimal.ONE;
        if (availableSeats != null && availableSeats <= config.getSeatScarcityThreshold()) {
            seatScarcityMultiplier = config.getSeatScarcityMultiplier();
        }

        PricingTemporaryRule activeRule = findFirstActiveRule(tripDate, effectiveTime);
        BigDecimal temporaryRuleMultiplier = BigDecimal.ONE;
        String appliedRuleName = null;
        if (activeRule != null) {
            temporaryRuleMultiplier = activeRule.getMultiplier();
            appliedRuleName = activeRule.getName();
        }

        BigDecimal suggestedPrice = basePrice
                .multiply(peakHourMultiplier)
                .multiply(heatMultiplier)
                .multiply(seatScarcityMultiplier)
                .multiply(temporaryRuleMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        if (suggestedPrice.compareTo(config.getMinPricePerSeat()) < 0) {
            suggestedPrice = config.getMinPricePerSeat();
        }
        if (suggestedPrice.compareTo(config.getMaxPricePerSeat()) > 0) {
            suggestedPrice = config.getMaxPricePerSeat();
        }

        BigDecimal adjustmentRatio = config.getDriverAdjustmentRatio();
        BigDecimal minAllowed = suggestedPrice
                .multiply(BigDecimal.ONE.subtract(adjustmentRatio))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxAllowed = suggestedPrice
                .multiply(BigDecimal.ONE.add(adjustmentRatio))
                .setScale(2, RoundingMode.HALF_UP);

        if (minAllowed.compareTo(config.getMinPricePerSeat()) < 0) {
            minAllowed = config.getMinPricePerSeat();
        }
        if (maxAllowed.compareTo(config.getMaxPricePerSeat()) > 0) {
            maxAllowed = config.getMaxPricePerSeat();
        }

        StringBuilder detail = new StringBuilder();
        detail.append("基础价: ").append(basePrice).append("元 (距离:").append(distance).append("km × ")
                .append(config.getBasePricePerKm()).append("元/km × 距离权重:").append(config.getDistanceWeight()).append(")");
        if (isPeakHour) {
            detail.append(" × 高峰系数(").append(peakHourMultiplier).append(")");
        } else {
            detail.append(" × 平峰系数(").append(peakHourMultiplier).append(")");
        }
        detail.append(" × 热度系数(").append(heatMultiplier).append(")");
        detail.append(" × 座位系数(").append(seatScarcityMultiplier).append(")");
        if (activeRule != null) {
            detail.append(" × 临时规则[").append(appliedRuleName).append("](").append(temporaryRuleMultiplier).append(")");
        }
        detail.append(" = ").append(suggestedPrice).append("元");

        SuggestedPriceResponse response = new SuggestedPriceResponse();
        response.setSuggestedPrice(suggestedPrice);
        response.setMinAllowedPrice(minAllowed);
        response.setMaxAllowedPrice(maxAllowed);
        response.setBasePrice(basePrice);
        response.setDistanceMultiplier(config.getDistanceWeight());
        response.setPeakHourMultiplier(peakHourMultiplier);
        response.setHeatMultiplier(heatMultiplier);
        response.setSeatScarcityMultiplier(seatScarcityMultiplier);
        response.setTemporaryRuleMultiplier(temporaryRuleMultiplier);
        response.setCalculationDetail(detail.toString());
        response.setPeakHour(isPeakHour);
        response.setRouteActiveCarpools(activeCarpools);
        response.setRouteRecentBookings(recentBookings);
        response.setRemainingSeats(availableSeats != null ? availableSeats : 0);
        response.setAppliedRuleName(appliedRuleName);
        response.setDriverAdjustmentRatio(adjustmentRatio);
        return response;
    }

    public void validatePriceWithinRange(BigDecimal finalPrice, BigDecimal suggestedPrice) {
        if (finalPrice == null || suggestedPrice == null) {
            return;
        }
        PricingConfig config = getOrCreateDefaultConfig();
        BigDecimal ratio = config.getDriverAdjustmentRatio();
        BigDecimal min = suggestedPrice.multiply(BigDecimal.ONE.subtract(ratio));
        BigDecimal max = suggestedPrice.multiply(BigDecimal.ONE.add(ratio));

        if (finalPrice.compareTo(min) < 0 || finalPrice.compareTo(max) > 0) {
            int percent = ratio.multiply(BigDecimal.valueOf(100)).intValue();
            throw new BusinessException(400,
                    "价格超出允许调整范围，只能在建议价±" + percent + "%范围内调整（"
                            + min.setScale(2, RoundingMode.HALF_UP) + " ~ "
                            + max.setScale(2, RoundingMode.HALF_UP) + "元）");
        }
    }

    public void ensureCarpoolPricing(Carpool carpool, Route route) {
        SuggestedPriceResponse suggested = calculateSuggestedPrice(
                route.getId(),
                carpool.getTripDate(),
                parseTime(route.getStartTime()),
                carpool.getAvailableSeats()
        );

        if (carpool.getSuggestedPricePerSeat() == null) {
            carpool.setSuggestedPricePerSeat(suggested.getSuggestedPrice());
        }

        if (carpool.getFinalPricePerSeat() == null) {
            carpool.setFinalPricePerSeat(suggested.getSuggestedPrice());
        } else {
            validatePriceWithinRange(carpool.getFinalPricePerSeat(), suggested.getSuggestedPrice());
        }
    }

    private LocalTime resolveTripTime(Route route, LocalTime explicitTime) {
        if (explicitTime != null) {
            return explicitTime;
        }
        LocalTime routeStart = parseTime(route.getStartTime());
        if (routeStart != null) {
            return routeStart;
        }
        return LocalTime.of(8, 0);
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getEffectiveDistance(Route route) {
        if (route.getAverageDistanceKm() != null && route.getAverageDistanceKm().compareTo(BigDecimal.ZERO) > 0) {
            return route.getAverageDistanceKm();
        }
        return DEFAULT_DISTANCE_KM;
    }

    private boolean isPeakHour(LocalDate date, LocalTime time) {
        DayOfWeek day = date.getDayOfWeek();
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        if (!isWeekday) {
            return false;
        }
        boolean isMorningPeak = !time.isBefore(MORNING_PEAK_START) && !time.isAfter(MORNING_PEAK_END);
        boolean isEveningPeak = !time.isBefore(EVENING_PEAK_START) && !time.isAfter(EVENING_PEAK_END);
        return isMorningPeak || isEveningPeak;
    }

    private PricingTemporaryRule findFirstActiveRule(LocalDate date, LocalTime time) {
        List<PricingTemporaryRule> rules = ruleRepository.findActiveRulesForDateTime(date, time);
        return rules.isEmpty() ? null : rules.get(0);
    }

    private void validateRuleDates(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
    }

    private void validateRuleTimes(LocalTime start, LocalTime end) {
        if (start == null && end == null) {
            return;
        }
        if ((start == null) != (end == null)) {
            throw new BusinessException(400, "开始时间和结束时间必须同时设置或同时为空");
        }
        if (start != null && start.isAfter(end)) {
            throw new BusinessException(400, "开始时间不能晚于结束时间");
        }
    }

    private PricingConfigResponse toConfigResponse(PricingConfig config) {
        PricingConfigResponse resp = new PricingConfigResponse();
        BeanUtils.copyProperties(config, resp);
        return resp;
    }

    private PricingTemporaryRuleResponse toRuleResponse(PricingTemporaryRule rule) {
        PricingTemporaryRuleResponse resp = new PricingTemporaryRuleResponse();
        BeanUtils.copyProperties(rule, resp);
        return resp;
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onApplicationReady() {
        getOrCreateDefaultConfig();
    }
}
