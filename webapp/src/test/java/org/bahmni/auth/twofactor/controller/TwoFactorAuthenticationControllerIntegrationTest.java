package org.bahmni.auth.twofactor.controller;


import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.bahmni.auth.twofactor.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
@TestExecutionListeners({DbUnitTestExecutionListener.class})
@TestPropertySource(properties = {"OTP_EXPIRES_AFTER = 1", "OTP_LENGTH = 6"})
@ActiveProfiles("test")
@DatabaseSetup("dataset.xml")
public class TwoFactorAuthenticationControllerIntegrationTest {

    private RestOperations restOperations = new RestTemplate();

    @Test
    public void shouldSendOTPToTheGiveNumber() {
        String response = restOperations.getForObject("http://localhost:8080/send?userName=user1", String.class);

        assertThat(response, is("true"));
    }

    @Test
    public void shouldNotSendOTPIfTheUserHasNoContact() {
        String response = restOperations.getForObject("http://localhost:8080/send?userName=randomUser", String.class);

        assertThat(response, is("false"));
    }

    @Test
    public void shouldReturnFalseForValidationWithWrongOTP() {
        String response = restOperations.getForObject("http://localhost:8080/validate?userName=user2&otp=abcd*^&*&^%$", String.class);

        assertThat(response, is("false"));
    }

    @Test
    public void shouldReturnFalseForValidationWithWrongUser() {
        String response = restOperations.getForObject("http://localhost:8080/validate?userName=randomUser&otp=abcd*^&*&^%$", String.class);

        assertThat(response, is("false"));
    }
}
