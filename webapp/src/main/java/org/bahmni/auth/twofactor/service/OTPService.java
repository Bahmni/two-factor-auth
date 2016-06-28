package org.bahmni.auth.twofactor.service;

import org.bahmni.auth.twofactor.model.OTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Service
public class OTPService {
    @Autowired
    private OTPConfiguration otpConfiguration;

    private SecureRandom secureRandom = new SecureRandom();
    private Map<String, OTP> hashMap = new HashMap<>();

    public OTP generateAndSaveOtpFor(String userName) {
        OTP otp = new OTP(generateOTP(), System.currentTimeMillis());
        hashMap.put(userName, otp);
        return otp;
    }

    private String generateOTP() {
        byte[] random = new byte[otpConfiguration.getOTPLength()];
        secureRandom.nextBytes(random);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : random) {
            stringBuilder.append(Math.abs(b % 10));
        }
        return stringBuilder.toString();
    }

    public boolean validateOTPFor(String userName, String receivedOtp) {
        OTP otp = hashMap.get(userName);
        if (otp != null && otp.toString().equals(receivedOtp)) {
            if (otp.isExpired(otpConfiguration.getExpiryTimeIntervalInMillis())) {
                return false;
            }
            hashMap.remove(userName);
            return true;
        }
        return false;
    }
}
