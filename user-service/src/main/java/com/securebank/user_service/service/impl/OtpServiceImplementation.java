package com.securebank.user_service.service.impl;


import com.securebank.user_service.config.TwilioConfig;
import com.securebank.user_service.entity.OtpPurpose;
import com.securebank.user_service.entity.OtpVerification;
import com.securebank.user_service.entity.User;
import com.securebank.user_service.exception.BadRequestException;
import com.securebank.user_service.exception.ResourceNotFoundException;
import com.securebank.user_service.repository.OtpVerificationRepository;
import com.securebank.user_service.repository.UserRepository;
import com.securebank.user_service.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpServiceImplementation implements OtpService {
    private final UserRepository userRepository;
    private final TwilioConfig twilioConfig;
    private final OtpVerificationRepository otpVerificationRepository;
    private final SmsServiceImplementation smsServiceImplementation;

    @Override
    @Transactional
    public void sendOtp(Long userId,OtpPurpose purpose, String pendingValue) {
        //Find the user with given user ID.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id: " + userId));
        // Delete any previous unused OTPs for this purpose
        otpVerificationRepository.deleteAllByUserIdAndPurpose(userId,purpose);
        //Generate the OTP
        String otpCode = generateOtp();
        //Save the OTP records to DB
        OtpVerification otpVerification = OtpVerification
                .builder()
                .user(user)
                .otpCode(otpCode)
                .purpose(purpose)
                .pendingValue(pendingValue)
                .expiresAt(LocalDateTime.now()
                        .plusMinutes(twilioConfig.getOtpExpiryMinutes()))
                .used(false)
                .build();
        otpVerificationRepository.save(otpVerification);
        //Send SMS to customers user mobile number
        // If updating phone number , send otp to the new phone number
            String PhoneNumber = (purpose==OtpPurpose.CHANGE_PHONE)
                    ?formatPhone(pendingValue)
                    :formatPhone(user.getPhoneNumber());

//        String toPhoneNumber =  formatPhone(user.getPhoneNumber());

        smsServiceImplementation.sendOtp(PhoneNumber,otpCode,purpose.name());
        log.info("OTP sent for userId: {} purpose: {}", userId, purpose);

    }


    @Override
    public String validateOtp(Long userId, OtpPurpose purpose, String otpCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id: " + userId));
        OtpVerification savedOtp = otpVerificationRepository
                .findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(user.getId(),purpose)
                .orElseThrow(() -> new RuntimeException("No active OTP found. Please request a new OTP."));
        if(savedOtp.isExpired()){
                throw new BadRequestException("OTP has expired. Please request a new one.");
        }
        else if(savedOtp.isUsed()){
            throw new BadRequestException("Please try with the recent OTP sent");
        }
        else if(!savedOtp.getOtpCode().equals(otpCode)){
            throw new BadRequestException("Invalid OTP. Please try again.");
        }
        savedOtp.setUsed(true);
        otpVerificationRepository.save(savedOtp);
        log.info("OTP validated successfully for userId: {} purpose: {}", userId, purpose);
        return savedOtp.getPendingValue();
    }

    // Helper Methods

    public String generateOtp() {

        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));   // 6 digit otp
    }

    public String formatPhone(String phoneNumber) {
        return "+91" + phoneNumber;
    }


}
