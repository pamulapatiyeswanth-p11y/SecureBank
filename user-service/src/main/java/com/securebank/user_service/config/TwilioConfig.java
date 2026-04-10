package com.securebank.user_service.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TwilioConfig {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @Value("${twilio.otp-expiry-minutes}")
    private int otpExpiryMinutes;

// Spring calls this method automatically after the bean is created and all dependencies are injected
    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }



}
