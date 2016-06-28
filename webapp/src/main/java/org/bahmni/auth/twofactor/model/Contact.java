package org.bahmni.auth.twofactor.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "contact")
public class Contact {

    @Id
    @Column(name = "user_name")
    private String userName;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "mobile_number")
    private String mobileNumber;

    public String getCountryCode() {
        return countryCode;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}

