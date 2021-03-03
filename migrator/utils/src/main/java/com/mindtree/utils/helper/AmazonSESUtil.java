package com.mindtree.utils.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.mindtree.utils.constants.MigratorConstants;
import com.mindtree.utils.exception.MigratorServiceException;

public class AmazonSESUtil {
	
	private AmazonSESUtil() {
		
	}

	private static AmazonSimpleEmailService sesClient;
	
	private static final  Logger LOGGER = LoggerFactory.getLogger(AmazonSESUtil.class);
	// This address must be verified with Amazon SES.
	private static final String FROM = "onkar.vijayvithal@mindtree.com";

	// is still in the sandbox, this address must be verified.
	private static final String TO = "onkar.vijayvithal@mindtree.com";

	private static String date = MigrationUtils.getDate();
	// The subject line for the email.
	
	private static StringBuilder SUBJECT = new StringBuilder("ELC : Asset Migration Validation Report - ").append(date);

	// The HTML body for the email.
	private static final String HTMLBODY = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
			+ "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
			+ "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>" + "AWS SDK for Java</a>";

	// The email body for recipients with non-HTML email clients.
	private static final String TEXTBODY = "This email was sent through Amazon SES " + "using the AWS SDK for Java.";
	
	// The email body for recipients with non-HTML email clients.
	private static String BODY_TEXT = "Hello,\r\n" + "Please see the attached file for a list "
			+ "of customers to contact.";

	// The HTML body of the email.
	private static String BODY_HTML = "<html>" + "<head></head>" + "<body>" + "<p>Hello Team!</p>"
			+ "<p>Please find the attached report for asset migrated in AEM.</p>" 
			+ "<p>Thanks,</p>"
			+ "<p>ELC Team</p>"
			+ "</body>" + "</html>";

	private static AmazonSimpleEmailService getAmazonSesInstance() throws MigratorServiceException {
		try {
			if (null == sesClient) {
				sesClient = AmazonSimpleEmailServiceClientBuilder.standard().withClientConfiguration(MigrationUtils.getAWSClientConfiguration())
						.withCredentials(MigrationUtils.getAWSCredentials()).withRegion(Regions.US_EAST_1).build();
			}
		} catch (AmazonServiceException ase) {
			LOGGER.error(
					"Caught an AmazonServiceException, which means your request made to Amazon SES, but was rejected with an error response.");
			LOGGER.error("Error Message: " + ase.getMessage() + ", HTTP Status Code: " + ase.getStatusCode()
					+ ", AWS Error Code:   " + ase.getErrorCode());
			throw new MigratorServiceException(
					"getAmazonSesInstance: request made to Amazon SES was rejected with an error response: " + ase);
		} catch (AmazonClientException ace) {
			LOGGER.error(
					"The client encountered an internal error while trying to communicate with SES, such as not being able to access the network. Error Message: "
							+ ace.getMessage());
			throw new MigratorServiceException("getAmazonSesInstance: internal error while trying to communicate with S3 with an error response: " + ace);
		}
		return sesClient;
	}
	
	public static void sendMail() {
		LOGGER.info("Vaidator : { sendMail- Mail sending Starts. }");
		try {
			SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withToAddresses(TO))
					.withMessage(new Message()
							.withBody(new Body().withHtml(new Content().withCharset(MigratorConstants.UTF_8).withData(HTMLBODY))
									.withText(new Content().withCharset(MigratorConstants.UTF_8).withData(TEXTBODY)))
							.withSubject(new Content().withCharset(MigratorConstants.UTF_8).withData(SUBJECT.toString())))
					.withSource(FROM);
			getAmazonSesInstance().sendEmail(request);
			LOGGER.info("Email sent!");
		} catch (Exception ex) {
			LOGGER.error("The email was not sent. Error message: ", ex);
		} finally {
			LOGGER.error("The email was not sent. ");
		}
	}
	
	public static void sendMail(String filePath, String toAddress, String fromAddress) {
		LOGGER.info("Vaidator : { sendMail- Mail sending Starts, fromAddress: {}, toAddress: {} }", fromAddress, toAddress);
		try {
			String DefaultCharSet = MimeUtility.getDefaultJavaCharset();

			Session session = Session.getDefaultInstance(new Properties());

			// Create a new MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Add subject, from and to lines.
			message.setSubject(SUBJECT.toString(), MigratorConstants.UTF_8);
			message.setFrom(new InternetAddress(fromAddress));
			message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(toAddress));

			// Create a multipart/alternative child container.
			MimeMultipart msg_body = new MimeMultipart("alternative");

			// Create a wrapper for the HTML and text parts.
			MimeBodyPart wrap = new MimeBodyPart();

			// Define the text part.
			MimeBodyPart textPart = new MimeBodyPart();
			// Encode the text content and set the character encoding. This step is
			// necessary if you're sending a message with characters outside the
			// ASCII range.
			textPart.setContent(MimeUtility.encodeText(BODY_TEXT, DefaultCharSet, "B"), "text/plain; charset=UTF-8");
			textPart.setHeader("Content-Transfer-Encoding", "base64");

			// Define the HTML part.
			MimeBodyPart htmlPart = new MimeBodyPart();
			// Encode the HTML content and set the character encoding.
			htmlPart.setContent(MimeUtility.encodeText(BODY_HTML, DefaultCharSet, "B"), "text/html; charset=UTF-8");
			htmlPart.setHeader("Content-Transfer-Encoding", "base64");

			// Add the text and HTML parts to the child container.
			msg_body.addBodyPart(textPart);
			msg_body.addBodyPart(htmlPart);

			// Add the child container to the wrapper object.
			wrap.setContent(msg_body);

			// Create a multipart/mixed parent container.
			MimeMultipart msg = new MimeMultipart("mixed");

			// Add the parent container to the message.
			message.setContent(msg);

			// Add the multipart/alternative part to the message.
			msg.addBodyPart(wrap);

			// Define the attachment
			MimeBodyPart att = new MimeBodyPart();
			DataSource fds = new FileDataSource(filePath);
			att.setDataHandler(new DataHandler(fds));
			att.setFileName(fds.getName());

			// Add the attachment to the message.
			msg.addBodyPart(att);

			// Try to send the email.
			LOGGER.info("Attempting to send an email through Amazon SES " + "using the AWS SDK for Java...");

			// Instantiate an Amazon SES client, which will make the service
			// call with the supplied AWS credentials.
			AmazonSimpleEmailService client = AmazonSESUtil.getAmazonSesInstance();

			// Send the email.
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			message.writeTo(outputStream);
			RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

			SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);

			client.sendRawEmail(rawEmailRequest);
			LOGGER.info("Email sent!");
			// Display an error if something goes wrong.
		} catch (AddressException e) {
			LOGGER.error("AddressException: The email was not sent. Error message: ", e);
		} catch (MessagingException e) {
			LOGGER.error("MessagingException: The email was not sent. Error message: ", e);
		} catch (IOException e) {
			LOGGER.error("IOException: The email was not sent. Error message: ", e);
		} catch (MigratorServiceException e) {
			LOGGER.error("MigratorServiceException: The email was not sent. Error message: ", e);
		} catch (Exception e) {
			LOGGER.error("Exception: The email was not sent. Error message: ", e);
		}

	}
}
