package org.bahmni.auth.twofactor.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.auth.twofactor.ResponseConstants;
import org.bahmni.auth.twofactor.model.OTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {
    private static Logger logger = LogManager.getLogger(OTPService.class);

    private Map<String, OTP> generatedOtps = new ConcurrentHashMap<>();
    private Map<String, Integer> otpAttempts = new ConcurrentHashMap<>();
    private SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private OTPConfiguration otpConfiguration;

    public OTP generateAndSaveOtpFor(String userName) {
        OTP otp = new OTP(generateOTP(), System.currentTimeMillis());
        generatedOtps.put(userName, otp);
        logger.info("OTP " + otp + " generated for " + userName);
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

    public String validateOTPFor(String userName, String receivedOtp) {
        OTP otp = generatedOtps.get(userName);

        if (otp != null) {
            if (otp.toString().equals(receivedOtp)) {
                if (otp.isExpired(otpConfiguration.getExpiryTimeIntervalInMillis())) {
                    logger.warn("Expired OTP " + receivedOtp + " sent by " + userName);
                    return ResponseConstants.FAILED;
                }
                generatedOtps.remove(userName);
                otpAttempts.remove(userName);
                logger.info("OTP " + receivedOtp + " validation successful for " + userName);
                return ResponseConstants.SUCCESS;
            } else {
                Integer attempts = otpAttempts.get(userName);
                if (attempts == null) {
                    attempts = 0;
                }
                attempts++;
                if (attempts > otpConfiguration.getMaxOTPAttempts()) {
                    otpAttempts.remove(userName);
                    logger.error(userName + " locked out for max otp attempts");
                    return ResponseConstants.LOCKED_OUT;
                }
                otpAttempts.put(userName, attempts);
                logger.warn("Failed attempt #" + attempts + " using OTP " + receivedOtp + " by " + userName);
            }
        } else {
            logger.error("OTP " + receivedOtp + " sent by " + userName + " is not generated by the system");
        }
        return ResponseConstants.FAILED;
    }
}
