package org.bahmni.auth.twofactor.model;

public class OTP {
    private String otp;
    private long generatedTimeInMillis;

    public OTP(String otp, long generatedTimeInMillis) {
        this.otp = otp;
        this.generatedTimeInMillis = generatedTimeInMillis;
    }

    @Override
    public String toString() {
        return otp;
    }

    public boolean isExpired(long expiryIntervalInMillis) {
        return System.currentTimeMillis() - generatedTimeInMillis > expiryIntervalInMillis;
    }
}
