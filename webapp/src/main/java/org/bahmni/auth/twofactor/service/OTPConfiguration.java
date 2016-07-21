package org.bahmni.auth.twofactor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class OTPConfiguration {

    @Autowired
    private Environment environment;

    public long getExpiryTimeIntervalInMillis() {
        return Integer.valueOf(environment.getProperty("OTP_EXPIRES_AFTER")) * 60 * 1000;
    }

    public int getOTPLength() {
        return Integer.valueOf(environment.getProperty("OTP_LENGTH"));
    }

    public int getMaxOTPAttempts() {
        return Integer.valueOf(environment.getProperty("OTP_MAX_ATTEMPTS"));
    }

    public int getMaxResendAttempts() {
        return Integer.valueOf(environment.getProperty("OTP_MAX_RESEND_ATTEMPTS"));
    }
}
