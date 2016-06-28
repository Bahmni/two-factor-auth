package org.bahmni.auth.twofactor.database;

import org.bahmni.auth.twofactor.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Database extends JpaRepository<Contact, Integer> {

    Contact findMobileNumberByUserName(@Param("user_name") String userName);

}
