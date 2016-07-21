package org.bahmni.auth.twofactor.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.bahmni.auth.twofactor.ResponseConstants;
import org.bahmni.auth.twofactor.model.OTP;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OTPService.class, OTP.class})
@PowerMockIgnore("javax.management.*")
public class OTPServiceTest {

    @Mock
    private OTPConfiguration otpConfiguration;

    @Mock
    private Appender appender;

    @InjectMocks
    private OTPService otpService;

    @Captor
    private ArgumentCaptor<LogEvent> captor;

    private Logger logger;

    @Before
    public void setUp() {
        Mockito.when(appender.getName()).thenReturn("MockAppender");
        Mockito.when(appender.isStarted()).thenReturn(true);
        Mockito.when(appender.isStopped()).thenReturn(false);

        logger = (Logger) LogManager.getLogger(OTPService.class);
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        mockStatic(System.class);
        when(System.currentTimeMillis()).thenReturn(12L);
        when(otpConfiguration.getOTPLength()).thenReturn(4);
        when(otpConfiguration.getExpiryTimeIntervalInMillis()).thenReturn(100L);
        when(otpConfiguration.getMaxOTPAttempts()).thenReturn(3);
        when(otpConfiguration.getMaxResendAttempts()).thenReturn(3);
    }

    @After
    public void tearDown() {
        logger.removeAppender(appender);
    }

    @Test
    public void shouldGenerateOTP() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        assertThat(otp.toString(), is(not(nullValue())));
    }

    @Test
    public void shouldReturnFalseIfThereIsNoOTPGeneratedForAUser() {
        String response = otpService.validateOTPFor("user", "123");

        assertThat(response, is(ResponseConstants.FAILED));
    }

    @Test
    public void shouldReturnFalseIfTheGivenOTPIsNotValidForAUser() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        String response = otpService.validateOTPFor("user", otp.toString() + "121");

        assertThat(response, is(ResponseConstants.FAILED));
    }

    @Test
    public void shouldReturnTrueIfTheGivenOTPIsValidForAUser() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        String response = otpService.validateOTPFor("user", otp.toString());

        assertThat(response, is(ResponseConstants.SUCCESS));
    }

    @Test
    public void shouldReturnFalseIfTheGivenOTPIsExpired() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        when(System.currentTimeMillis()).thenReturn(1290L);

        String response = otpService.validateOTPFor("user", otp.toString());

        assertThat(response, is(ResponseConstants.EXPIRED));
    }


    @Test
    public void shouldReturnTrueIfTheGivenOTPIsCorrectAndNotExpired() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        when(System.currentTimeMillis()).thenReturn(90L);

        String response = otpService.validateOTPFor("user", otp.toString());

        assertThat(response, is(ResponseConstants.SUCCESS));
    }

    @Test
    public void generatedOtpShouldBeRandomForAUserToGuessIt() throws InterruptedException {
        Set<String> generatedOtps = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            OTP otp = otpService.generateAndSaveOtpFor("user");
            if (generatedOtps.contains(otp.toString())) {
                Assert.fail("Same OTP generated again after " + x + " OTPs!");
            }
            Thread.sleep(100L);
            generatedOtps.add(otp.toString());
        }
    }

    @Test
    public void shouldLogOutUserIfInvalidAttemptsAreMoreThanConfiguredLimit() {
        OTP otp = otpService.generateAndSaveOtpFor("user");
        String wrongOTP = otp.toString() + "121";

        String response = otpService.validateOTPFor("user", wrongOTP);
        assertThat(response, is(ResponseConstants.FAILED));

        response = otpService.validateOTPFor("user", wrongOTP);
        assertThat(response, is(ResponseConstants.FAILED));

        response = otpService.validateOTPFor("user", wrongOTP);
        assertThat(response, is(ResponseConstants.MAX_ATTEMPTS_EXCEEDED));
    }

    @Test
    public void comprehensiveTestForSendOTP() {
        OTP otp = otpService.generateAndSaveOtpFor("user");
        String wrongOTP = otp.toString() + "121";

        otpService.validateOTPFor("user", wrongOTP);
        otpService.validateOTPFor("user", wrongOTP);
        otpService.validateOTPFor("user", wrongOTP);

        OTP otp2 = otpService.generateAndSaveOtpFor("user");
        String wrongOTP2 = otp2.toString() + "121";
        otpService.validateOTPFor("user", wrongOTP2);
        String response = otpService.validateOTPFor("user", otp2.toString());

        assertThat(response, is(ResponseConstants.SUCCESS));
        verifyLogMessages("OTP " + otp.toString() + " generated for user", "Failed attempt #1 using OTP " + wrongOTP + " by user", "Failed attempt #2 using OTP " + wrongOTP + " by user", "Failed attempt #3 using OTP " + wrongOTP + " by user", "Max failed OTP attempts exceeded for user", "OTP " + otp2.toString() + " generated for user", "Failed attempt #1 using OTP " + wrongOTP2 + " by user", "OTP " + otp2.toString() + " validation successful for user");
    }

    @Test
    public void shouldLogFailedAndMaxInvalidAttemptsEvents() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        String wrongOTP = otp.toString() + "121";

        otpService.validateOTPFor("user", wrongOTP);
        otpService.validateOTPFor("user", wrongOTP);
        otpService.validateOTPFor("user", wrongOTP);

        verifyLogMessages("OTP " + otp.toString() + " generated for user", "Failed attempt #1 using OTP " + wrongOTP + " by user", "Failed attempt #2 using OTP " + wrongOTP + " by user", "Failed attempt #3 using OTP " + wrongOTP + " by user", "Max failed OTP attempts exceeded for user");
    }

    @Test
    public void shouldLogWheneverOTPIsGenerated() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        verify(appender, times(1)).append(captor.capture());
        assertThat(captor.getValue().getMessage().getFormattedMessage(), is("OTP " + otp.toString() + " generated for user"));
    }

    @Test
    public void shouldLogWarningIfOTPIsExpired() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        when(System.currentTimeMillis()).thenReturn(1290L);
        otpService.validateOTPFor("user", otp.toString());

        verifyLogMessages("OTP " + otp.toString() + " generated for user", "Expired OTP " + otp.toString() + " sent by user");
    }

    @Test
    public void shouldLogSuccessfulValidationOfOTP() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        otpService.validateOTPFor("user", otp.toString());

        verifyLogMessages("OTP " + otp.toString() + " generated for user", "OTP " + otp.toString() + " validation successful for user");
    }

    @Test
    public void shouldLogFailedValidationOfOTP() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        String wrongOTP = otp.toString() + "123";
        otpService.validateOTPFor("user", wrongOTP);

        verifyLogMessages("OTP " + otp.toString() + " generated for user", "Failed attempt #1 using OTP " + wrongOTP + " by user");
    }

    @Test
    public void shouldLogOTPValidationAttemptsIfOTPIsNotGenerated() {
        otpService.validateOTPFor("user", "123");

        verifyLogMessages("OTP 123 sent by user is not generated by the system");
    }

    @Test
    public void shouldResendOTP() {
        OTP otp = otpService.regenerateAndSaveOtpFor("username");

        assertThat(otp, is(not(nullValue())));

        verifyLogMessages("Resend attempt #1 OTP " + otp.toString() + " re-generated for username");
    }

    @Test
    public void shouldValidateResentOTP() {
        OTP otp = otpService.regenerateAndSaveOtpFor("userName");

        String response = otpService.validateOTPFor("userName", otp.toString());

        assertThat(response, is(ResponseConstants.SUCCESS));

        verifyLogMessages("Resend attempt #1 OTP " + otp.toString() + " re-generated for userName", "OTP " + otp.toString() + " validation successful for userName");
    }

    @Test
    public void shouldReturnNullAfterExceedingMaxResendAttempts() {
        OTP otp1 = otpService.regenerateAndSaveOtpFor("userName");
        OTP otp2 = otpService.regenerateAndSaveOtpFor("userName");
        OTP otp3 = otpService.regenerateAndSaveOtpFor("userName");
        OTP otp4 = otpService.regenerateAndSaveOtpFor("userName");

        assertThat(otp1, is(not(nullValue())));
        assertThat(otp2, is(not(nullValue())));
        assertThat(otp3, is(not(nullValue())));
        assertThat(otp4, is(nullValue()));

        verifyLogMessages("Resend attempt #1 OTP " + otp1.toString() + " re-generated for userName", "Resend attempt #2 OTP " + otp2.toString() + " re-generated for userName", "Resend attempt #3 OTP " + otp3.toString() + " re-generated for userName", "Max resend attempts exceeded by userName");
    }

    @Test
    public void shouldValidateTheOTPGeneratedAtLastAllowedAttempt() {
        OTP otp1 = otpService.regenerateAndSaveOtpFor("userName");
        OTP otp2 = otpService.regenerateAndSaveOtpFor("userName");
        OTP otp3 = otpService.regenerateAndSaveOtpFor("userName");

        String response = otpService.validateOTPFor("userName", otp3.toString());

        assertThat(response, is(ResponseConstants.SUCCESS));

        verifyLogMessages("Resend attempt #1 OTP " + otp1.toString() + " re-generated for userName", "Resend attempt #2 OTP " + otp2.toString() + " re-generated for userName", "Resend attempt #3 OTP " + otp3.toString() + " re-generated for userName", "OTP " + otp3.toString() + " validation successful for userName");
    }

    @Test
    public void comprehensiveTestForReSendingOTP() {
        OTP otp1 = otpService.regenerateAndSaveOtpFor("user");
        OTP otp2 = otpService.regenerateAndSaveOtpFor("user");
        OTP otp3 = otpService.regenerateAndSaveOtpFor("user");
        otpService.regenerateAndSaveOtpFor("user");

        OTP otp5 = otpService.generateAndSaveOtpFor("user");
        OTP otp6 = otpService.regenerateAndSaveOtpFor("user");

        String response = otpService.validateOTPFor("user", otp6.toString());
        assertThat(response, is(ResponseConstants.SUCCESS));

        verifyLogMessages("Resend attempt #1 OTP " + otp1.toString() + " re-generated for user",
                "Resend attempt #2 OTP " + otp2.toString() + " re-generated for user",
                "Resend attempt #3 OTP " + otp3.toString() + " re-generated for user",
                "Max resend attempts exceeded by user",
                "OTP " + otp5.toString() + " generated for user",
                "Resend attempt #1 OTP " + otp6.toString() + " re-generated for user",
                "OTP " + otp6.toString() + " validation successful for user");
    }

    private void verifyLogMessages(String... messages) {
        verify(appender, times(messages.length)).append(captor.capture());

        List<LogEvent> capturedValues = captor.getAllValues();
        assertThat(messages.length, is(capturedValues.size()));

        ListIterator<LogEvent> iterator = capturedValues.listIterator();
        while (iterator.hasNext()) {
            assertThat(iterator.next().getMessage().getFormattedMessage(), is(messages[iterator.nextIndex() - 1]));
        }
    }

    @Test
    public void shouldProperlyClearCountersAfterLoggingOutByMaxingResendOTPAttempts() {
        OTP otp1 = otpService.generateAndSaveOtpFor("user");

        String wrongOTP1 = otp1.toString() + "12";
        otpService.validateOTPFor("user", wrongOTP1);

        OTP otp2 = otpService.regenerateAndSaveOtpFor("user");
        OTP otp3 = otpService.regenerateAndSaveOtpFor("user");
        OTP otp4 = otpService.regenerateAndSaveOtpFor("user");
        otpService.regenerateAndSaveOtpFor("user");

        OTP otp6 = otpService.generateAndSaveOtpFor("user");

        String wrongOTP2 = otp6.toString() + "12";
        otpService.validateOTPFor("user", wrongOTP2);

        OTP otp7 = otpService.regenerateAndSaveOtpFor("user");

        verifyLogMessages("OTP " + otp1.toString() + " generated for user",
                "Failed attempt #1 using OTP " + wrongOTP1 + " by user",
                "Resend attempt #1 OTP " + otp2.toString() + " re-generated for user",
                "Resend attempt #2 OTP " + otp3.toString() + " re-generated for user",
                "Resend attempt #3 OTP " + otp4.toString() + " re-generated for user",
                "Max resend attempts exceeded by user",
                "OTP " + otp6.toString() + " generated for user",
                "Failed attempt #1 using OTP " + wrongOTP2 + " by user",
                "Resend attempt #1 OTP " + otp7.toString() + " re-generated for user");
    }


    @Test
    public void shouldProperlyClearCountersAfterLoggingOutByMaxingWrongOTPAttempts() {
        OTP otp1 = otpService.generateAndSaveOtpFor("user");

        String wrongOTP1 = otp1.toString() + "12";
        otpService.validateOTPFor("user", wrongOTP1);
        otpService.validateOTPFor("user", wrongOTP1);
        OTP otp2 = otpService.regenerateAndSaveOtpFor("user");
        otpService.validateOTPFor("user", wrongOTP1);

        OTP otp3 = otpService.generateAndSaveOtpFor("user");

        String wrongOTP2 = otp3.toString() + "12";
        otpService.validateOTPFor("user", wrongOTP2);

        OTP otp4 = otpService.regenerateAndSaveOtpFor("user");

        verifyLogMessages("OTP " + otp1.toString() + " generated for user",
                "Failed attempt #1 using OTP " + wrongOTP1 + " by user",
                "Failed attempt #2 using OTP " + wrongOTP1 + " by user",
                "Resend attempt #1 OTP " + otp2.toString() + " re-generated for user",
                "Failed attempt #3 using OTP " + wrongOTP1 + " by user",
                "Max failed OTP attempts exceeded for user",
                "OTP " + otp3.toString() + " generated for user",
                "Failed attempt #1 using OTP " + wrongOTP2 + " by user",
                "Resend attempt #1 OTP " + otp4.toString() + " re-generated for user");
    }

    @Test
    public void shouldProperlyClearCountersAfterLoggingOutByExpiryOfOTP() {
        OTP otp1 = otpService.generateAndSaveOtpFor("user");

        String wrongOTP1 = otp1.toString() + "12";
        otpService.validateOTPFor("user", wrongOTP1);

        OTP otp2 = otpService.regenerateAndSaveOtpFor("user");

        when(System.currentTimeMillis()).thenReturn(120L);
        otpService.validateOTPFor("user", otp2.toString());

        OTP otp3 = otpService.generateAndSaveOtpFor("user");

        String wrongOTP2 = otp3.toString() + "12";
        otpService.validateOTPFor("user", wrongOTP2);

        OTP otp4 = otpService.regenerateAndSaveOtpFor("user");

        verifyLogMessages("OTP " + otp1.toString() + " generated for user",
                "Failed attempt #1 using OTP " + wrongOTP1 + " by user",
                "Resend attempt #1 OTP " + otp2.toString() + " re-generated for user",
                "Expired OTP " + otp2.toString() + " sent by user",
                "OTP " + otp3.toString() + " generated for user",
                "Failed attempt #1 using OTP " + wrongOTP2 + " by user",
                "Resend attempt #1 OTP " + otp4.toString() + " re-generated for user");
    }
}