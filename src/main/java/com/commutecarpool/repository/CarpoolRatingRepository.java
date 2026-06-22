package com.commutecarpool.repository;

import com.commutecarpool.entity.CarpoolRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CarpoolRatingRepository extends JpaRepository<CarpoolRating, Long> {

    Page<CarpoolRating> findByCarpoolId(Long carpoolId, Pageable pageable);

    Page<CarpoolRating> findByReviewerId(Long reviewerId, Pageable pageable);

    Page<CarpoolRating> findByRevieweeId(Long revieweeId, Pageable pageable);

    List<CarpoolRating> findByRevieweeId(Long revieweeId);

    @Query("SELECT AVG(r.rating) FROM CarpoolRating r WHERE r.revieweeId = :revieweeId")
    Double getAverageRatingForUser(@Param("revieweeId") Long revieweeId);

    @Query("SELECT AVG(r.rating) FROM CarpoolRating r WHERE r.revieweeId = :revieweeId AND r.createdAt >= :since")
    Double getAverageRatingForUserSince(@Param("revieweeId") Long revieweeId, @Param("since") LocalDateTime since);

    Page<CarpoolRating> findByCarpoolIdAndReviewerId(Long carpoolId, Long reviewerId, Pageable pageable);

    Page<CarpoolRating> findByCarpoolIdAndRevieweeId(Long carpoolId, Long revieweeId, Pageable pageable);

    Page<CarpoolRating> findByReviewerIdAndRevieweeId(Long reviewerId, Long revieweeId, Pageable pageable);

    Page<CarpoolRating> findByCarpoolIdAndReviewerIdAndRevieweeId(Long carpoolId, Long reviewerId, Long revieweeId, Pageable pageable);

    List<CarpoolRating> findByRevieweeIdAndCreatedAtAfter(Long revieweeId, LocalDateTime since);
}
