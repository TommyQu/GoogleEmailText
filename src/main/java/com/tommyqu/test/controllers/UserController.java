package com.tommyqu.test.controllers;

import java.security.Provider;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;
import com.tommyqu.test.utils.OAuth2Authenticator;
import com.tommyqu.test.utils.OAuth2SaslClientFactory;

@Controller
@RequestMapping(value="user")
public class UserController {
	
	@RequestMapping(value="login.do")
	public @ResponseBody String login(String idToken, String accessToken, HttpServletRequest request) {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
	    .setAudience(Arrays.asList("441689445364-62bigpadlctn2kvl0c377hm2oqdjqpfo.apps.googleusercontent.com"))
	    .setIssuer("accounts.google.com")
	    .build();
		try {
			System.out.println("ID token: " + idToken);
			GoogleIdToken gIdToken = verifier.verify(idToken);
			if (idToken != null) {
			  Payload payload = gIdToken.getPayload();
			  // Print user identifier
			  String userId = payload.getSubject();

			  // Get profile information from payload
			  String email = payload.getEmail();
			  boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
			  String name = (String) payload.get("name");
			  String pictureUrl = (String) payload.get("picture");
			  String locale = (String) payload.get("locale");
			  String familyName = (String) payload.get("family_name");
			  String givenName = (String) payload.get("given_name");
			  request.getSession().setAttribute("email", email);
			  request.getSession().setAttribute("accessToken", accessToken);
			  System.out.println("Email: " + email);
			  System.out.println("AccessToken: " + accessToken);
			  
			} else {
				return "Invalid ID token.";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "success";
	}
	
	@RequestMapping(value="sendEmail.do")
	public @ResponseBody String sendEmail(String toEmail, String subject, String content, HttpServletRequest request) {
		try {
			 String myEmail = (String) request.getSession().getAttribute("email");
			 String accessToken = (String) request.getSession().getAttribute("accessToken");
			 System.out.println("My Email: " + myEmail);
			 System.out.println("Send AccessToken: " + accessToken);
			 
			 OAuth2Authenticator oAuthenticator = new OAuth2Authenticator();
			 oAuthenticator.initialize();
		    Properties props = new Properties();
		    props.put("mail.smtp.starttls.enable", "true");
		    props.put("mail.smtp.starttls.required", "true");
		    props.put("mail.smtp.sasl.enable", "true");
		    props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");
		    props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, accessToken);
		    Session session = Session.getInstance(props);
		    session.setDebug(true);

		    MimeMessage mimeMessage = new MimeMessage(session);
		    mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
		    mimeMessage.setSubject(subject);
		    mimeMessage.setContent(content, "text/html");
		    
		    final URLName unusedUrlName = null;
		    SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
		    // If the password is non-null, SMTP tries to do AUTH LOGIN.
		    final String emptyPassword = "";
		    transport.connect("smtp.gmail.com", 587, myEmail, emptyPassword);
		    transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
		    transport.close();
		    return "success";
			 
			 
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage().toString();
		}

	}
	
}
