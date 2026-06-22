package com.commutecarpool.service;

import com.commutecarpool.dto.PageResponse;
import com.commutecarpool.dto.rating.RatingRequest;
import com.commutecarpool.dto.rating.RatingResponse;
import com.commutecarpool.entity.Carpool;
import com.commutecarpool.entity.CarpoolRating;
import com.commutecarpool.exception.BusinessException;
import com.commutecarpool.repository.CarpoolRatingRepository;
import com.commutecarpool.repository.CarpoolRepository;
import com.commutecarpool.security.SecurityUtils;
import com.commutecarpool.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final CarpoolRatingRepository carpoolRatingRepository;
    private final CarpoolRepository carpoolRepository;
    private final DriverCreditService driverCreditService;

    public PageResponse<RatingResponse> listRatings(Long carpoolId, Long reviewerId, Long revieweeId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<CarpoolRating> ratingPage;
        if (carpoolId != null && reviewerId != null && revieweeId != null) {
            ratingPage = carpoolRatingRepository.findByCarpoolIdAndReviewerIdAndRevieweeId(carpoolId, reviewerId, revieweeId, pageable);
        } else if (carpoolId != null && reviewerId != null) {
            ratingPage = carpoolRatingRepository.findByCarpoolIdAndReviewerId(carpoolId, reviewerId, pageable);
        } else if (carpoolId != null && revieweeId != null) {
            ratingPage = carpoolRatingRepository.findByCarpoolIdAndRevieweeId(carpoolId, revieweeId, pageable);
        } else if (reviewerId != null && revieweeId != null) {
            ratingPage = carpoolRatingRepository.findByReviewerIdAndRevieweeId(reviewerId, revieweeId, pageable);
        } else if (carpoolId != null) {
            ratingPage = carpoolRatingRepository.findByCarpoolId(carpoolId, pageable);
        } else if (reviewerId != null) {
            ratingPage = carpoolRatingRepository.findByReviewerId(reviewerId, pageable);
        } else if (revieweeId != null) {
            ratingPage = carpoolRatingRepository.findByRevieweeId(revieweeId, pageable);
        } else {
            ratingPage = carpoolRatingRepository.findAll(pageable);
        }
        return PageUtils.toPageResponse(ratingPage, RatingResponse.class);
    }

    public RatingResponse createRating(RatingRequest req) {
        carpoolRepository.findById(req.getCarpoolId())
                .orElseThrow(() -> new BusinessException(404, "拼车不存在"));
        if (SecurityUtils.getCurrentUserId().equals(req.getRevieweeId())) {
            throw new BusinessException(400, "不能评价自己");
        }
        CarpoolRating rating = new CarpoolRating();
        BeanUtils.copyProperties(req, rating);
        rating.setReviewerId(SecurityUtils.getCurrentUserId());
        carpoolRatingRepository.save(rating);

        try {
            driverCreditService.checkAndApplyRestrictions(req.getRevieweeId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "信用评估失败：" + e.getMessage());
        }

        RatingResponse response = new RatingResponse();
        BeanUtils.copyProperties(rating, response);
        return response;
    }

    public RatingResponse getRating(Long id) {
        CarpoolRating rating = carpoolRatingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "评价不存在"));
        RatingResponse response = new RatingResponse();
        BeanUtils.copyProperties(rating, response);
        return response;
    }

    public RatingResponse updateRating(Long id, RatingRequest req) {
        CarpoolRating rating = carpoolRatingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "评价不存在"));
        if (!rating.getReviewerId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权操作");
        }
        BeanUtils.copyProperties(req, rating);
        rating.setId(id);
        rating.setReviewerId(SecurityUtils.getCurrentUserId());
        carpoolRatingRepository.save(rating);
        RatingResponse response = new RatingResponse();
        BeanUtils.copyProperties(rating, response);
        return response;
    }

    public void deleteRating(Long id) {
        CarpoolRating rating = carpoolRatingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "评价不存在"));
        if (!rating.getReviewerId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(403, "无权操作");
        }
        carpoolRatingRepository.delete(rating);
    }
}
