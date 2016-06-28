package org.bahmni.auth.twofactor.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OTP.class})
public class OTPTest {

    @Test
    public void shouldReturnTheGeneratedOTP() {
        OTP otp = new OTP("1234", 98318);

        assertThat(otp.toString(), is("1234"));
    }

    @Test
    public void shouldReturnTrueIfTheOTPIsExpired() {
        OTP otp = new OTP("1234", 10);

        mockStatic(System.class);
        when(System.currentTimeMillis()).thenReturn(100L);

        assertThat(otp.isExpired(20), is(true));
    }

    @Test
    public void shouldReturnFalseIfTheOTPIsNotExpired() {
        OTP otp = new OTP("1234", 10);

        mockStatic(System.class);
        when(System.currentTimeMillis()).thenReturn(100L);

        assertThat(otp.isExpired(200), is(false));
    }
}