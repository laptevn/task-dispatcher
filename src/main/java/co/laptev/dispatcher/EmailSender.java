package co.laptev.dispatcher;

import co.laptev.dispatcher.entity.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;

class EmailSender {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final boolean SMTP_IS_AUTHENTICATED = true;
    private static final boolean SMTP_IS_TLS_ENABLED = true;

    private static final Logger logger = LogManager.getLogger(EmailSender.class);

    private final String fromEmail;
    private final String userName;
    private final String password;

    public EmailSender(String fromEmail, String userName, String password) {
        this.fromEmail = fromEmail;
        this.userName = userName;
        this.password = password;
    }

    public boolean send(String destinationEmail, String subject, String messageText, Optional<Attachment> attachment) {
        try {
            Message message = new MimeMessage(createSession());
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinationEmail));
            message.setSubject(subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(messageText, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            if (attachment.isPresent()) {
                multipart.addBodyPart(createAttachmentPart(attachment.get()));
            }

            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            logger.error("Could not send email", e);
            return false;
        } finally {
            if (attachment.isPresent()) {
                try {
                    attachment.get().getContent().close();
                } catch (IOException e) {
                    logger.error("Could not close a stream for a submitted file", e);
                }
            }
        }

        return true;
    }

    private static MimeBodyPart createAttachmentPart(Attachment attachment) throws MessagingException {
        try {
            Path tempFilePath = Files.createTempFile(null, null);
            Files.copy(attachment.getContent(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            File attachingFile = tempFilePath.toFile();
            attachingFile.deleteOnExit();

            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.attachFile(attachingFile);
            attachmentBodyPart.setFileName(attachment.getName());
            return attachmentBodyPart;
        } catch (IOException e) {
            throw new MessagingException("Could not attach file", e);
        }
    }

    private Session createSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", SMTP_HOST);
        prop.put("mail.smtp.port", SMTP_PORT);
        prop.put("mail.smtp.auth", SMTP_IS_AUTHENTICATED);
        prop.put("mail.smtp.starttls.enable", SMTP_IS_TLS_ENABLED);

        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
    }
}