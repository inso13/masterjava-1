package ru.javaops.masterjava.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.util.List;

/**
 * gkislin
 * 15.11.2016
 */
@Slf4j
public class MailSender {
    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        try {
            for (Addressee addressee:to) {
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
            email.send();}

        } catch (EmailException e) {
            e.printStackTrace();
        }

     //   log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled()?"\nbody=" + body:""));
    }
}
