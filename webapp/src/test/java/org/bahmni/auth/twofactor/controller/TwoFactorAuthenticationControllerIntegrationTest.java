package org.bahmni.auth.twofactor.controller;


import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.bahmni.auth.twofactor.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestExecutionListeners({DbUnitTestExecutionListener.class})
@TestPropertySource(properties = {"OTP_EXPIRES_AFTER = 1", "OTP_LENGTH = 6"})
@WebIntegrationTest("server.port:62480")
@ActiveProfiles("test")
@DatabaseSetup("dataset.xml")
public class TwoFactorAuthenticationControllerIntegrationTest {

    private final String SERVER_URL = "http://localhost:62480";
    private RestOperations restOperations = new RestTemplate();

    @Test
    public void shouldSendOTPToTheGiveNumber() {
        String response = restOperations.getForObject(SERVER_URL + "/send?userName=user1", String.class);

        assertThat(response, is("true"));
    }

    @Test
    public void shouldNotSendOTPIfTheUserHasNoContact() {
        String response = restOperations.getForObject(SERVER_URL + "/send?userName=randomUser", String.class);

        assertThat(response, is("false"));
    }

    @Test
    public void shouldReturnFalseForValidationWithWrongOTP() {
        String response = restOperations.getForObject(SERVER_URL + "/validate?userName=user2&otp=abcd*^&*&^%$", String.class);

        assertThat(response, is("false"));
    }

    @Test
    public void shouldReturnFalseForValidationWithWrongUser() {
        String response = restOperations.getForObject(SERVER_URL + "/validate?userName=randomUser&otp=abcd*^&*&^%$", String.class);

        assertThat(response, is("false"));
    }
}
