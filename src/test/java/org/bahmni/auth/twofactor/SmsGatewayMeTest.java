package org.bahmni.auth.twofactor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestOperations;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class SmsGatewayMeTest {

    private String sendMessageUrl = "https://smsgateway.me/api/v3/messages/send";
    private String deviceListUrl = "https://smsgateway.me/api/v3/devices";
    private String email = "abcd@gmail.com";
    private String password = "abcpass";

    @Mock
    private RestOperations restOperations;

    @Mock
    private SmsGatewayMeConfig config;

    @InjectMocks
    private SmsGatewayMe smsGatewayMe;

    @Test
    public void shouldSendMessageToSmsGateWayWithUserCredentials() {
        String request = String.format("email=%s&password=%s&message=%s&number=%s&device=%s", email, password, "Hello", "1234", "10");
        String deviceUrl = String.format(deviceListUrl + "?email=%s&password=%s", email, password);
        String deviceDetail = "{ result : { data: [ {'id':'10'}]}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(request, headers);
        when(restOperations.getForObject(deviceUrl, String.class)).thenReturn(deviceDetail);
        when(config.getEmail()).thenReturn("abcd@gmail.com");
        when(config.getPassword()).thenReturn("abcpass");

        smsGatewayMe.sendSMS("Hello", "1234");

        verify(restOperations, times(1)).getForObject(eq(deviceUrl), eq(String.class));
        verify(restOperations, times(1)).postForObject(eq(sendMessageUrl), eq(entity), eq(String.class));
    }

}