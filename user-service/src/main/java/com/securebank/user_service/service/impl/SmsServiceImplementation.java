package com.securebank.user_service.service.impl;

import com.securebank.user_service.config.TwilioConfig;
import com.securebank.user_service.service.SmsService;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Builder
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImplementation implements SmsService {

    private final TwilioConfig twilioConfig;
    @Override
    public void sendOtp(String toPhoneNumber, String otpCode, String purpose) {
        try{
            String messageBody = buildMessage(otpCode,purpose);
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioConfig.getFromNumber())
                    ,messageBody).create();
            log.info("SMS sent successfully. SID: {}", message.getSid());
        }
        catch (Exception e){
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send OTP. Please try again.");

        }
    }

    private String buildMessage(String otpCode, String purpose) {
        return switch (purpose) {
            case "CHANGE_EMAIL"    ->
                    "SecureBank: Your OTP to change email is " + otpCode +
                            ". Valid for 5 minutes. Do not share this with anyone.";
            case "CHANGE_PASSWORD" ->
                    "SecureBank: Your OTP to change password is " + otpCode +
                            ". Valid for 5 minutes. Do not share this with anyone.";
            case "CHANGE_PHONE"    ->
                    "SecureBank: Your OTP to change phone number is " + otpCode +
                            ". Valid for 5 minutes. Do not share this with anyone.";
            default ->
                    "SecureBank: Your OTP is " + otpCode +
                            ". Valid for 5 minutes. Do not share this with anyone.";
        };

    }
}
