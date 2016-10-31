/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.email;

import java.io.UnsupportedEncodingException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author svenkataramanappa
 */
public class Email {

    private static final Logger LOGGER = LogManager.getLogger(Email.class);
    private static String EMAIL = "java:comp/env/mail/mailSession";

    public static void sendEmailWithoutAttachment(String[] mailId, String subject, String messageBody, String from) throws UnsupportedEncodingException {
        try {
            String[] to = mailId;
            Context initCtx = new InitialContext();
            Session session = (Session) initCtx.lookup(EMAIL);

            String host = session.getProperty("mail.smtp.host");
            String user = session.getProperty("mail.smtp.user");
            String pass = session.getProperty("mail.smtp.pass");

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "noreply"));

            InternetAddress[] toAddress = new InternetAddress[to.length];
            for (int i = 0; i < to.length; i++) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for (int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);

            MimeBodyPart messagePart = new MimeBodyPart();
            messagePart.setText(messageBody);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messagePart);

            message.setContent(multipart);

            Transport transport = session.getTransport("smtp");
            transport.connect(host, user, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (NamingException ex) {
            LOGGER.info("Naming exception", ex);
        } catch (MessagingException ex) {
            LOGGER.info("Messaging exception", ex);
        }
    }
}
