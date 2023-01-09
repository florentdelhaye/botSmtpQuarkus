import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@QuarkusTest
public class BotTest {
    @Test
    public void testServer() throws MessagingException {

        String host = "localhost";
        String to = "recipient+open+click@gmail.com";//change accordingly

        //Get the session object
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "1967");
        props.put("mail.smtp.auth", "false");

        Session session = Session.getDefaultInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@gmail.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("javatpoint");
        message.setText("<html>" +
                "This is simple program of sending email using JavaMail API" +
                "<a href=\"img.domain.com/o/aezeza\" />" +
                "<a href=\"img.domain.com/c/aezeza\" />" +
                "<a href=\"img.domain.com/c/aezeza\" />" +
                "</html>");

        //send the message
        Transport.send(message);

        System.out.println("message sent successfully...");

    }

}
