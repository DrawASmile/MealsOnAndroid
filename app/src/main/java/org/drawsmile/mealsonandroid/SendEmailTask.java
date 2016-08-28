package org.drawsmile.mealsonandroid;

import android.os.AsyncTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by bhegd on 8/22/2016.
 */
public class SendEmailTask extends AsyncTask<String, Void, String> {



    @Override
    protected String doInBackground(String... name) {

        String firstName = "N/A";
        String lastName = "N/A";

        int count = 0 ;
        for (String n : name) {
            if(count == 0)
            {
                firstName = n;
                count++;
            }
            else
            {
                lastName = n;
            }

        }


        final String username = "volsignup@drawsmile.org";
        final String password = "Volsignup1234";

        Properties props = new Properties();

        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "mail.drawsmile.org");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("volsignup@drawsmile.org"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("bharath@drawsmile.org"));
            message.setSubject("Volunteer Signup: " + firstName + " " + lastName);
            message.setText(firstName + "\n" + lastName + "\n\nSigned up to volunteer at the Draw a Smile food service!");

            Transport.send(message);

            Log.i("javamail", "Done sending email");




        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }


        return "";
    }

    protected void onPostExecute(Long result) {


    }
}
