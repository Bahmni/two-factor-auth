package org.bahmni.auth.twofactor.controller;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.bahmni.auth.smsinterface.SmsGateWay;
import org.bahmni.auth.twofactor.ResponseConstants;
import org.bahmni.auth.twofactor.database.Database;
import org.bahmni.auth.twofactor.model.Contact;
import org.bahmni.auth.twofactor.model.OTP;
import org.bahmni.auth.twofactor.service.OTPConfiguration;
import org.bahmni.auth.twofactor.service.OTPService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TwoFactorAuthenticationControllerTest {

    @Mock
    private OTPConfiguration otpConfiguration;

    @Mock
    private SmsGateWay smsGateWay;

    @Mock
    private Database database;

    @Mock
    private OTPService otpService;

    @Mock
    private Appender appender;

    @Captor
    private ArgumentCaptor<LogEvent> captor;

    @InjectMocks
    private TwoFactorAuthenticationController twoFactorAuthenticationController;

    private Logger logger;

    @Before
    public void setUp() {
        when(appender.getName()).thenReturn("MockAppender");
        when(appender.isStarted()).thenReturn(true);
        when(appender.isStopped()).thenReturn(false);

        logger = (Logger) LogManager.getLogger(TwoFactorAuthenticationController.class);
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
    }

    @After
    public void tearDown() {
        logger.removeAppender(appender);
    }

    @Test
    public void shouldSendSMSToTheNumberFetchedFromTheDB() {
        Contact contact = new Contact();
        contact.setUserName("user");
        contact.setCountryCode("91");
        contact.setMobileNumber("909099172");
        when(otpService.generateAndSaveOtpFor("user")).thenReturn(new OTP("1234", 123121));
        when(database.findMobileNumberByUserName("user")).thenReturn(contact);

        boolean messageSent = twoFactorAuthenticationController.sendOTP("user");

        verify(smsGateWay, times(1)).sendSMS("91", "909099172", "1234");
        assertThat(messageSent, is(true));
    }

    @Test
    public void shouldReturnFalseIfThereIsNoContactAvailableForTheUser() {
        when(otpService.generateAndSaveOtpFor("user")).thenReturn(new OTP("1234", 123121));

        boolean messageSent = twoFactorAuthenticationController.sendOTP("user");

        assertThat(messageSent, is(false));
    }

    @Test
    public void shouldReturnTrueIfOtpServiceValidationSucceeds() {
        when(otpService.validateOTPFor("user", "1234")).thenReturn(ResponseConstants.SUCCESS);

        String response = twoFactorAuthenticationController.validateOTP("user", "1234");

        verify(otpService, times(1)).validateOTPFor("user", "1234");
        assertThat(response, is(ResponseConstants.SUCCESS));
    }

    @Test
    public void shouldReturnFalseIfOtpServiceValidationFails() {
        when(otpService.validateOTPFor("user", "1234")).thenReturn(ResponseConstants.FAILED);

        String response = twoFactorAuthenticationController.validateOTP("user", "1234");

        verify(otpService, times(1)).validateOTPFor("user", "1234");
        assertThat(response, is(ResponseConstants.FAILED));
    }

    @Test
    public void shouldReturnMaxOTPAttemptsReachedIfValidationForOTPAttemptsFails() {
        when(otpService.validateOTPFor("user", "1234")).thenReturn(ResponseConstants.MAX_ATTEMPTS_EXCEEDED);

        String response = twoFactorAuthenticationController.validateOTP("user", "1234");

        verify(otpService, times(1)).validateOTPFor("user", "1234");
        assertThat(response, is(ResponseConstants.MAX_ATTEMPTS_EXCEEDED));
    }

    @Test
    public void shouldLogAfterSendingSMSToTheUser() {
        Contact contact = new Contact();

        contact.setUserName("user");
        contact.setCountryCode("91");
        contact.setMobileNumber("909099172");
        when(otpService.generateAndSaveOtpFor("user")).thenReturn(new OTP("1234", 123121));
        when(database.findMobileNumberByUserName("user")).thenReturn(contact);

        twoFactorAuthenticationController.sendOTP("user");

        verify(appender, times(1)).append(captor.capture());
        assertThat(captor.getValue().getMessage().getFormattedMessage(), is("SMS sent to user carrying OTP 1234"));
    }

    @Test
    public void shouldSendSMSWhenResendingOTP() {
        Contact contact = new Contact();
        contact.setUserName("user");
        contact.setCountryCode("91");
        contact.setMobileNumber("909099172");
        when(otpService.regenerateAndSaveOtpFor("user")).thenReturn(new OTP("1234", 123121));
        when(database.findMobileNumberByUserName("user")).thenReturn(contact);

        String messageSent = twoFactorAuthenticationController.resendOTP("user");

        verify(smsGateWay, times(1)).sendSMS("91", "909099172", "1234");
        assertThat(messageSent, is("true"));
    }

    @Test
    public void shouldReturnMaxAttemptsExceeded() {
        Contact contact = new Contact();
        contact.setUserName("user");
        contact.setCountryCode("91");
        contact.setMobileNumber("909099172");
        when(otpService.regenerateAndSaveOtpFor("user")).thenReturn(null);
        when(database.findMobileNumberByUserName("user")).thenReturn(contact);

        String messageSent = twoFactorAuthenticationController.resendOTP("user");

        verify(smsGateWay, times(0)).sendSMS(anyString(), anyString(), anyString());
        assertThat(messageSent, is(ResponseConstants.MAX_RESEND_ATTEMPTS_EXCEEDED));
    }
}