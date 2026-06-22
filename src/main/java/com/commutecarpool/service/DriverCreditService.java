package com.commutecarpool.service;

import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.restriction.RestrictionLogResponse;
import com.commutecarpool.dto.restriction.RestrictionResponse;
import com.commutecarpool.dto.restriction.RestrictionReviewAppealRequest;
import com.commutecarpool.dto.restriction.RestrictionSubmitAppealRequest;
import com.commutecarpool.entity.AppealStatus;
import com.commutecarpool.entity.CarpoolRating;
import com.commutecarpool.entity.DriverRestriction;
import com.commutecarpool.entity.DriverRestrictionLog;
import com.commutecarpool.entity.RestrictionStatus;
import com.commutecarpool.entity.RestrictionType;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.CarpoolRatingRepository;
import com.commutecarpool.repository.CarpoolRepository;
import com.commutecarpool.repository.DriverRestrictionRepository;
import com.commutecarpool.security.SecurityUtils;
import com.commutecarpool.util.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverCreditService {

    private final DriverRestrictionRepository restrictionRepository;
    private final CarpoolRatingRepository ratingRepository;
    private final CarpoolRepository carpoolRepository;

    private static final int RESTRICTION_DAYS = 7;
    private static final int COMPLAINT_THRESHOLD = 3;
    private static final double LOW_RATING_THRESHOLD = 3.0;
    private static final int COMPLAINT_RATING_MAX = 2;

    public boolean isDriverRestricted(Long driverId) {
        return !restrictionRepository.findActiveRestrictions(driverId, LocalDateTime.now()).isEmpty();
    }

    public List<DriverRestriction> getActiveRestrictions(Long driverId) {
        return restrictionRepository.findActiveRestrictions(driverId, LocalDateTime.now());
    }

    @Transactional
    public void checkAndApplyRestrictions(Long driverId) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        checkComplaints(driverId, oneMonthAgo);
        checkLowRating(driverId);
        checkNoShow(driverId, oneMonthAgo);
    }

    private void checkComplaints(Long driverId, LocalDateTime since) {
        List<CarpoolRating> lowRatings = ratingRepository.findByRevieweeIdAndCreatedAtAfter(driverId, since).stream()
                .filter(r -> r.getRating() != null && r.getRating() <= COMPLAINT_RATING_MAX)
                .collect(Collectors.toList());

        if (lowRatings.size() > COMPLAINT_THRESHOLD) {
            long existingCount = restrictionRepository.countByDriverIdAndTypeSince(driverId, RestrictionType.TOO_MANY_COMPLAINTS, since);
            if (existingCount == 0) {
                createRestriction(driverId, RestrictionType.TOO_MANY_COMPLAINTS,
                        String.format("近一个月内收到%d次有效投诉（评分≤2），超过阈值%d次", lowRatings.size(), COMPLAINT_THRESHOLD));
            }
        }
    }

    private void checkLowRating(Long driverId) {
        LocalDateTime since = LocalDateTime.now().minusMonths(1);
        Double avgRating = ratingRepository.getAverageRatingForUserSince(driverId, since);
        if (avgRating != null && avgRating < LOW_RATING_THRESHOLD) {
            long existingCount = restrictionRepository.countByDriverIdAndTypeSince(driverId, RestrictionType.LOW_RATING, since);
            if (existingCount == 0) {
                createRestriction(driverId, RestrictionType.LOW_RATING,
                        String.format("近一个月平均评分%.1f分，低于阈值%.1f分", avgRating, LOW_RATING_THRESHOLD));
            }
        }
    }

    private void checkNoShow(Long driverId, LocalDateTime since) {
        long noShowCount = carpoolRepository.countNoShowByDriverIdSince(driverId, since);
        if (noShowCount > 0) {
            long existingCount = restrictionRepository.countByDriverIdAndTypeSince(driverId, RestrictionType.NO_SHOW, since);
            if (existingCount == 0) {
                createRestriction(driverId, RestrictionType.NO_SHOW,
                        String.format("近一个月内出现%d次爽约行为", noShowCount));
            }
        }
    }

    @Transactional
    public DriverRestriction createRestriction(Long driverId, RestrictionType type, String reason) {
        LocalDateTime now = LocalDateTime.now();

        DriverRestriction restriction = new DriverRestriction();
        restriction.setDriverId(driverId);
        restriction.setRestrictionType(type);
        restriction.setStatus(RestrictionStatus.ACTIVE);
        restriction.setReason(reason);
        restriction.setStartTime(now);
        restriction.setEndTime(now.plusDays(RESTRICTION_DAYS));
        restriction.setAppealStatus(AppealStatus.NONE);

        DriverRestrictionLog restrictionLog = createLog(restriction, null, RestrictionStatus.ACTIVE, reason, driverId);
        restriction.addLog(restrictionLog);

        restrictionRepository.save(restriction);
        log.info("司机{}被限制，类型：{}，原因：{}", driverId, type, reason);
        return restriction;
    }

    @Transactional
    public RestrictionResponse submitAppeal(Long restrictionId, RestrictionSubmitAppealRequest req) {
        Long driverId = SecurityUtils.getCurrentUserId();
        DriverRestriction restriction = restrictionRepository.findById(restrictionId)
                .orElseThrow(() -> new BusinessException(404, "限制记录不存在"));

        if (!restriction.getDriverId().equals(driverId)) {
            throw new BusinessException(403, "无权操作");
        }

        if (restriction.getStatus() != RestrictionStatus.ACTIVE) {
            throw new BusinessException(400, "当前限制已失效，无需申诉");
        }

        if (restriction.getAppealStatus() == AppealStatus.PENDING) {
            throw new BusinessException(400, "已提交申诉，请等待审核");
        }

        if (restriction.getAppealStatus() == AppealStatus.APPROVED) {
            throw new BusinessException(400, "申诉已通过");
        }

        restriction.setAppealStatus(AppealStatus.PENDING);
        restriction.setAppealReason(req.getAppealReason());
        restriction.setAppealMaterial(req.getAppealMaterial());
        restriction.setAppealSubmittedAt(LocalDateTime.now());

        DriverRestrictionLog restrictionLog = createLog(restriction, restriction.getStatus(), restriction.getStatus(),
                "司机提交申诉：" + req.getAppealReason(), driverId);
        restriction.addLog(restrictionLog);

        restrictionRepository.save(restriction);
        return toResponse(restriction);
    }

    @Transactional
    public RestrictionResponse reviewAppeal(Long restrictionId, RestrictionReviewAppealRequest req) {
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }

        Long reviewerId = SecurityUtils.getCurrentUserId();
        DriverRestriction restriction = restrictionRepository.findById(restrictionId)
                .orElseThrow(() -> new BusinessException(404, "限制记录不存在"));

        if (restriction.getAppealStatus() != AppealStatus.PENDING) {
            throw new BusinessException(400, "该限制记录无待审核的申诉");
        }

        AppealStatus newAppealStatus;
        try {
            newAppealStatus = AppealStatus.valueOf(req.getAppealStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的申诉状态");
        }

        if (newAppealStatus != AppealStatus.APPROVED && newAppealStatus != AppealStatus.REJECTED) {
            throw new BusinessException(400, "申诉审核状态必须为 APPROVED 或 REJECTED");
        }

        RestrictionStatus oldStatus = restriction.getStatus();
        restriction.setAppealStatus(newAppealStatus);
        restriction.setReviewerId(reviewerId);
        restriction.setReviewRemark(req.getRemark());
        restriction.setReviewedAt(LocalDateTime.now());

        if (newAppealStatus == AppealStatus.APPROVED) {
            restriction.setStatus(RestrictionStatus.LIFTED);
            DriverRestrictionLog restrictionLog = createLog(restriction, oldStatus, RestrictionStatus.LIFTED,
                    "管理员审核通过申诉，提前解除限制" + (req.getRemark() != null ? "：" + req.getRemark() : ""), reviewerId);
            restriction.addLog(restrictionLog);
            log.info("司机{}的限制被管理员提前解除", restriction.getDriverId());
        } else {
            DriverRestrictionLog restrictionLog = createLog(restriction, oldStatus, oldStatus,
                    "管理员驳回申诉" + (req.getRemark() != null ? "：" + req.getRemark() : ""), reviewerId);
            restriction.addLog(restrictionLog);
        }

        restrictionRepository.save(restriction);
        return toResponse(restriction);
    }

    @Transactional
    public void expireRestrictions() {
        LocalDateTime now = LocalDateTime.now();
        List<DriverRestriction> expiredRestrictions = restrictionRepository.findExpiredActiveRestrictions(RestrictionStatus.ACTIVE, now);

        for (DriverRestriction restriction : expiredRestrictions) {
            RestrictionStatus oldStatus = restriction.getStatus();
            restriction.setStatus(RestrictionStatus.EXPIRED);

            DriverRestrictionLog restrictionLog = createLog(restriction, oldStatus, RestrictionStatus.EXPIRED,
                    "限制期限到期，自动解除", 0L);
            restriction.addLog(restrictionLog);
            restrictionRepository.save(restriction);
        }
    }

    public RestrictionResponse getMyActiveRestriction() {
        Long driverId = SecurityUtils.getCurrentUserId();
        List<DriverRestriction> restrictions = restrictionRepository.findActiveRestrictions(driverId, LocalDateTime.now());
        if (restrictions.isEmpty()) {
            return null;
        }
        return toResponse(restrictions.get(0));
    }

    public List<RestrictionResponse> getMyRestrictionHistory(int page, int size) {
        Long driverId = SecurityUtils.getCurrentUserId();
        PageRequest pageable = PageRequest.of(page, size);
        Page<DriverRestriction> restrictionPage = restrictionRepository.findByDriverId(driverId, pageable);
        return restrictionPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PageResponse<RestrictionResponse> listRestrictions(String status, String type, int page, int size) {
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }
        PageRequest pageable = PageRequest.of(page, size);
        Page<DriverRestriction> restrictionPage;

        if (status != null && type != null) {
            RestrictionStatus statusEnum = RestrictionStatus.valueOf(status.toUpperCase());
            RestrictionType typeEnum = RestrictionType.valueOf(type.toUpperCase());
            restrictionPage = restrictionRepository.findByStatusAndRestrictionType(statusEnum, typeEnum, pageable);
        } else if (status != null) {
            RestrictionStatus statusEnum = RestrictionStatus.valueOf(status.toUpperCase());
            restrictionPage = restrictionRepository.findByStatus(statusEnum, pageable);
        } else if (type != null) {
            RestrictionType typeEnum = RestrictionType.valueOf(type.toUpperCase());
            restrictionPage = restrictionRepository.findByRestrictionType(typeEnum, pageable);
        } else {
            restrictionPage = restrictionRepository.findAll(pageable);
        }

        List<RestrictionResponse> content = restrictionPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageUtils.toPageResponse(restrictionPage, content);
    }

    public RestrictionResponse getRestriction(Long id) {
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }
        DriverRestriction restriction = restrictionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "限制记录不存在"));
        return toResponse(restriction);
    }

    public List<RestrictionLogResponse> getRestrictionLogs(Long id) {
        if (!SecurityUtils.isAdmin()) {
            throw new BusinessException(403, "无权限执行此操作");
        }
        DriverRestriction restriction = restrictionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "限制记录不存在"));
        return restriction.getLogs().stream()
                .map(this::toLogResponse)
                .collect(Collectors.toList());
    }

    private DriverRestrictionLog createLog(DriverRestriction restriction,
                                           RestrictionStatus oldStatus,
                                           RestrictionStatus newStatus,
                                           String remark,
                                           Long operatorId) {
        DriverRestrictionLog restrictionLog = new DriverRestrictionLog();
        restrictionLog.setRestriction(restriction);
        restrictionLog.setOldStatus(oldStatus);
        restrictionLog.setNewStatus(newStatus);
        restrictionLog.setRemark(remark);
        restrictionLog.setOperatorId(operatorId);
        return restrictionLog;
    }

    private RestrictionResponse toResponse(DriverRestriction restriction) {
        RestrictionResponse response = new RestrictionResponse();
        BeanUtils.copyProperties(restriction, response);
        response.setRestrictionType(restriction.getRestrictionType().name());
        response.setStatus(restriction.getStatus().name());
        response.setAppealStatus(restriction.getAppealStatus().name());
        if (restriction.getLogs() != null) {
            response.setLogs(restriction.getLogs().stream()
                    .map(this::toLogResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    private RestrictionLogResponse toLogResponse(DriverRestrictionLog restrictionLog) {
        RestrictionLogResponse response = new RestrictionLogResponse();
        BeanUtils.copyProperties(restrictionLog, response);
        response.setOldStatus(restrictionLog.getOldStatus() != null ? restrictionLog.getOldStatus().name() : null);
        response.setNewStatus(restrictionLog.getNewStatus().name());
        return response;
    }
}
