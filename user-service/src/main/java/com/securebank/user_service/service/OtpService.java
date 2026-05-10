package com.securebank.user_service.service;

import com.securebank.user_service.entity.OtpPurpose;

public interface OtpService {

    void sendOtp(Long userId, OtpPurpose purpose, String pendingValue);

    String validateOtp(Long userId, OtpPurpose purpose, String otpCode);
}