package ru.javaops.masterjava.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.skife.jdbi.v2.DBI;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.dao.EmailDao;
import ru.javaops.masterjava.persist.model.EmailResult;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * gkislin
 * 15.11.2016
 */
@Slf4j
public class MailSender {



     static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
         DBI dbi = new DBI("jdbc:postgresql://localhost:5432/masterjava", "user", "password");
         EmailDao emailDao = dbi.open(EmailDao.class);

            for (Addressee addressee:to) {
                try {
            Email email = new SimpleEmail();
            email.setHostName("smtp.yandex.ru");
            email.setSmtpPort(465);
            email.setAuthenticator(new DefaultAuthenticator("nedis89", "V22gh7rpm"));
            email.setSSLOnConnect(true);
            email.setDebug(true);
            email.setFrom("nedis89@yandex.ru");
            email.setSubject("TestMail");
            email.setMsg("This is a test mail ... :-)");
            email.addTo(addressee.getEmail());
            email.send();
                EmailResult emailResult = new EmailResult("success", LocalDateTime.now(), addressee.getEmail());
             emailDao.insertGeneratedId(emailResult);}

            catch (Exception e) {
                        EmailResult emailResult = new EmailResult("fail", LocalDateTime.now(), addressee.getEmail());
                        emailDao.insertGeneratedId(emailResult);
                        e.printStackTrace();
                    }
            }

        }

     //   log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled()?"\nbody=" + body:""));

}
