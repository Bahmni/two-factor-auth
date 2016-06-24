package org.bahmni.auth.twofactor;

import org.bahmni.auth.smsinterface.SmsGateWay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EnableAutoConfiguration
@RestController
public class TwoFactorAuthenticationController {

    @Autowired
    private SmsGateWay smsGateWay;

    private Random random = new Random();
    private Map<String, String> hashMap = new HashMap<>();

    @RequestMapping(path = "/send", method = RequestMethod.GET)
    public void sendOTP(@RequestParam(name = "userName") String userName) {
        String otp = String.valueOf((int) (random.nextFloat() * 100000));
        hashMap.put(userName, otp);
        smsGateWay.sendSMS("91", "8985292480", otp);
    }

    @RequestMapping(path = "/validate", method = RequestMethod.GET)
    public boolean validateOTP(@RequestParam(name = "userName") String userName, @RequestParam(name = "otp") String otp) {
        String otpGenerated = hashMap.get(userName);
        if (otpGenerated != null) {
            if (otpGenerated.equals(otp)) {
                hashMap.remove(userName);
                return true;
            }
        }
        return false;
    }
}
