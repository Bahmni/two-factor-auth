package org.bahmni.auth.twofactor;

import org.springframework.stereotype.Service;

@Service
public interface SmsGateWay {

    void sendSMS(String message, String mobileNumber);
}
