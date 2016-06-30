package org.bahmni.auth.twofactor.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
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

        verifyErrorMessages("OTP " + otp.toString() + " generated for user", "Expired OTP " + otp.toString() + " sent by user");
    }

    @Test
    public void shouldLogSuccessfulValidationOfOTP() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        otpService.validateOTPFor("user", otp.toString());

        verifyErrorMessages("OTP " + otp.toString() + " generated for user", "OTP " + otp.toString() + " validation successful for user");
    }

    @Test
    public void shouldLogFailedValidationOfOTP() {
        OTP otp = otpService.generateAndSaveOtpFor("user");

        otpService.validateOTPFor("user", otp.toString() + "123");

        verifyErrorMessages("OTP " + otp.toString() + " generated for user", "OTP " + otp.toString() + "123" + " validation failed for user");
    }

    @Test
    public void shouldLogOTPValidationAttemptsIfOTPIsNotGenerated() {
        otpService.validateOTPFor("user", "123");

        verifyErrorMessages("OTP 123 sent by user is not generated by the system");
    }

    private void verifyErrorMessages(String... messages) {
        verify(appender, times(messages.length)).append(captor.capture());

        List<LogEvent> capturedValues = captor.getAllValues();
        assertThat(messages.length, is(capturedValues.size()));

        ListIterator<LogEvent> iterator = capturedValues.listIterator();
        while (iterator.hasNext()) {
            assertThat(messages[iterator.nextIndex()], is(iterator.next().getMessage().getFormattedMessage()));
        }
    }

}