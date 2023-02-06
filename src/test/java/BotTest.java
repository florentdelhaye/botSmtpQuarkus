import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.smtp.SMTPTransport;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;

@QuarkusTest
public class BotTest {
    @Test
    public void testServer() throws Exception {

        List<Integer> tasks = new ArrayList<>();
        for(int i=0; i<1; i++)
            tasks.add(i, i);

        Properties prop = new Properties();
        prop.put("mail.smtp.auth", false);
        prop.put("mail.smtp.starttls.enable", "false");
        prop.put("mail.smtp.host", "localhost");
        prop.put("mail.smtp.port", "1967");
        prop.put("mail.smtp.ssl.trust", "");
        prop.put("mail.smtp.sendpartial", true);

        Session session = Session.getInstance(prop);
        Address[] addresses = InternetAddress.parse(
                "to@gmail.com, to+open@gmail.com, to+click@gmail.com");

        Instant now = Instant.now();
        System.out.println("BEGIN : " + now);

        new ForkJoinPool(1024).submit(() -> {
            tasks.stream().parallel().forEach(integer -> {
                try {
                    SMTPTransport smtpTransport = new SMTPTransport(session, new URLName("http://localhost:1967"));
                    smtpTransport.connect();

                    smtpTransport.setUseRset(true);
                    smtpTransport.sendMessage(getMessage(session, addresses), addresses);
                    smtpTransport.sendMessage(getMessage(session, addresses), addresses);
                    smtpTransport.sendMessage(getMessage(session, addresses), addresses);
                    smtpTransport.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }).get();

        Instant end = Instant.now();
        System.out.println("END : " + end);
        System.out.println("Time : " + (end.getNano() - now.getNano()));
    }

    private static SMTPMessage getMessage(Session session, Address[] addresses) throws Exception {
        SMTPMessage message = new SMTPMessage(session);
        message.setFrom(new InternetAddress("from@gmail.com"));
        message.setRecipients(Message.RecipientType.TO, addresses);
        message.setSubject("Mail Subject");
        message.setSentDate(Date.from(Instant.now()));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder = stringBuilder.append("<html>").append("\n");
        stringBuilder = stringBuilder.append("<body>").append("\n");
        stringBuilder = stringBuilder.append("This is my first email using JavaMailer").append("\n");
        stringBuilder = stringBuilder.append("<a href=\"http://localhost:8089/tracking/o/\"/>").append("\n");
        stringBuilder = stringBuilder.append("This is my first email using JavaMailer").append("\n");
        stringBuilder = stringBuilder.append("<img src=\"http://localhost:8089/tracking/c/\"/>").append("\n");
        stringBuilder = stringBuilder.append("</body>").append("\n");
        stringBuilder = stringBuilder.append("</html>").append("\n");
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setText(stringBuilder.toString(), "utf-8");


        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        return message;
    }

}
