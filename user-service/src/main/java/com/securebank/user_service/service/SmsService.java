package com.securebank.user_service.service;

public interface SmsService {

    void sendOtp(String toPhoneNumber, String otpCode, String purpose);
}
