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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OTP otp1 = (OTP) o;

        if (generatedTimeInMillis != otp1.generatedTimeInMillis) return false;
        return otp != null ? otp.equals(otp1.otp) : otp1.otp == null;

    }

    @Override
    public int hashCode() {
        int result = otp != null ? otp.hashCode() : 0;
        result = 31 * result + (int) (generatedTimeInMillis ^ (generatedTimeInMillis >>> 32));
        return result;
    }
}
