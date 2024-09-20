package com.example.appv1;

import android.app.Activity;
import android.widget.Toast;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MyEmailSender {

    public static void sendReportEmail(Activity activity, String userEmail, String opinionId) {
        new Thread(() -> {
            // Configurar las propiedades para la conexión SMTP
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "465");

            // Iniciar sesión con las credenciales del correo electrónico auxiliar
            Session session = Session.getDefaultInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(EMAIL, PASSWORD);
                        }
                    });

            // Crear el mensaje de correo electrónico
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("contacto@historiacapital.site")); // Correo de destino
                message.setSubject("Reporte de Reseña");
                message.setText("El usuario con correo " + userEmail + " ha reportado la reseña con ID: " + opinionId);

                // Enviar el correo electrónico
                Transport.send(message);
                showToastOnUiThread(activity, "Reseña reportada exitosamente.");
            } catch (MessagingException e) {
                e.printStackTrace();
                showToastOnUiThread(activity, "Error al reportar la reseña.");
            }
        }).start();
    }

    private static void showToastOnUiThread(Activity activity, String message) {
        // Mostrar un Toast en el hilo principal (UI thread)
        if (activity != null) {
            activity.runOnUiThread(() -> Toast.makeText(activity, message, Toast.LENGTH_SHORT).show());
        }
    }
}
