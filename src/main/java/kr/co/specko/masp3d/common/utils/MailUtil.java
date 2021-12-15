package kr.co.specko.masp3d.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Component
public class MailUtil {

    public void sendTemplateMail(String toMail, String subject, String fromName,String contents)
            throws Exception {

        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost("smtp.naver.com");
        javaMailSender.setPort(587);
        javaMailSender.setUsername("maps3d@naver.com");
        javaMailSender.setPassword("capa1999#");

        InternetAddress from = new InternetAddress("maps3d@naver.com", fromName);
        InternetAddress to = new InternetAddress(toMail);


        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

        messageHelper.setFrom(from);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(contents, true);

        javaMailSender.send(mimeMessage);
    }
}
