package org.bahmni.auth.smsinterface;

public interface SmsGateWay {
    void sendSMS(String message, String mobileNumber);
}
