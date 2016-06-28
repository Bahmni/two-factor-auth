package org.bahmni.auth.twofactor.service;

import org.bahmni.auth.twofactor.model.OTP;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OTPService.class, OTP.class})
public class OTPServiceTest {

    @Mock
    private OTPConfiguration otpConfiguration;

    @InjectMocks
    private OTPService otpService;

    @Before
    public void setUp() {
        mockStatic(System.class);
        when(System.currentTimeMillis()).thenReturn(12L);
        when(otpConfiguration.getOTPLength()).thenReturn(4);
        when(otpConfiguration.getExpiryTimeIntervalInMillis()).thenReturn(100L);
    }

    @Test
    public void shouldGenerateOTP() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        assertThat(otp.toString(), is(not(nullValue())));
    }

    @Test
    public void shouldReturnFalseIfThereIsNoOTPGeneratedForAUser() {
        boolean isValid = otpService.validateOTPFor("user", "123");

        assertThat(isValid, is(false));
    }

    @Test
    public void shouldReturnFalseIfTheGivenOTPIsNotValidForAUser() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        boolean isValid = otpService.validateOTPFor("user", otp.toString() + "121");

        assertThat(isValid, is(false));
    }

    @Test
    public void shouldReturnTrueIfTheGivenOTPIsValidForAUser() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        boolean isValid = otpService.validateOTPFor("user", otp.toString());

        assertThat(isValid, is(true));
    }

    @Test
    public void shouldReturnFalseIfTheGivenOTPIsExpired() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        when(System.currentTimeMillis()).thenReturn(1290L);

        boolean isValid = otpService.validateOTPFor("user", otp.toString());

        assertThat(isValid, is(false));
    }


    @Test
    public void shouldReturnTrueIfTheGivenOTPIsCorrectAndNotExpired() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        when(System.currentTimeMillis()).thenReturn(90L);

        boolean isValid = otpService.validateOTPFor("user", otp.toString());

        assertThat(isValid, is(true));
    }

}