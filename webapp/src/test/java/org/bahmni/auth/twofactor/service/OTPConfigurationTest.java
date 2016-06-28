package org.bahmni.auth.twofactor.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OTPConfigurationTest {
    @Mock
    private Environment environment;

    @InjectMocks
    private OTPConfiguration otpConfiguration;

    @Test
    public void shouldSetCorrectConfiguration() {
        when(environment.getProperty("OTP_EXPIRES_AFTER")).thenReturn("10");
        when(environment.getProperty("OTP_LENGTH")).thenReturn("6");

        assertThat(otpConfiguration.getExpiryTimeIntervalInMillis(), is(600000L));
        assertThat(otpConfiguration.getOTPLength(), is(6));
    }
}