package com.securebank.user_service.repository;

import com.securebank.user_service.entity.OtpPurpose;
import com.securebank.user_service.entity.OtpVerification;
import com.securebank.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification,Long> {

    // Find latest OTP for the user+purpose which is not used yet
    Optional<OtpVerification> findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(Long userId,OtpPurpose purpose);
    // Delete all previous OTPs for a user+purpose before creating a new one
    void deleteAllByUserIdAndPurpose(Long userId,OtpPurpose purpose);

}
