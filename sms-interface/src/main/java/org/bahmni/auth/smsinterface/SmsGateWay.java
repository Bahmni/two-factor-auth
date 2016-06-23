package org.bahmni.auth.smsinterface;

public interface SmsGateWay {
    void sendSMS(String countryCode, String mobileNumber, String message);
}
