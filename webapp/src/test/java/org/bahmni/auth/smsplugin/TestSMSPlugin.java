package org.bahmni.auth.smsplugin;

import org.bahmni.auth.smsinterface.SmsGateWay;
import org.springframework.stereotype.Component;

@Component
public class TestSMSPlugin implements SmsGateWay {

    @Override
    public void sendSMS(String countryCode, String mobileNumber, String message) {
    }
}
