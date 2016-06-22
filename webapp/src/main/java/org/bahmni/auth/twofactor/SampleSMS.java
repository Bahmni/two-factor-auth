package org.bahmni.auth.twofactor;

import org.bahmni.auth.smsinterface.SmsGateWay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableAutoConfiguration
@RestController
public class SampleSMS {

    @Autowired
    SmsGateWay smsGateWay;

    @RequestMapping("/")
    public void hello() {
        smsGateWay.sendSMS("abcd", "89891289");
    }
}
