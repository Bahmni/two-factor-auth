package org.bahmni.auth.twofactor;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

@Component
public class SmsGatewayMeConfig {

    private Properties props;

    public SmsGatewayMeConfig() {
        this(System.getProperty("user.home") + File.separator + ".bahmni-security" + File.separator
                , "SmsGatewayMe.conf");
    }

    private SmsGatewayMeConfig(String directory, String file) {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(directory + file);
            props = new Properties();
            props.load(new InputStreamReader(inputStream));
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getEmail() {
        return props.getProperty("email");
    }

    public String getPassword() {
        return props.getProperty("password");
    }
}
