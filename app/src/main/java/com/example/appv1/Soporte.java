package com.example.appv1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.appv1.R;


public class Soporte extends AppCompatActivity {

    private static final String TAG = "Soporte";
    public ImageView btnBackarrow;
    public ImageView imageFoto;
    public TextView answerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soporte);

        btnBackarrow = findViewById(R.id.btnBackarrow);
        imageFoto = findViewById(R.id.contacto_soporte_foto);
        answerTextView = findViewById(R.id.answer1); // Asigna un valor predeterminado

        // Configura la justificación para answerTextView si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            answerTextView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        btnBackarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Soporte.this, Homepage.class);
                startActivity(intent);
                finish();
            }
        });

        imageFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear el `Intent` para enviar un correo
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:contacto@historiacapital.site"));
                // Especificar el asunto y el cuerpo del correo si es necesario
                intent.putExtra(Intent.EXTRA_SUBJECT, "Dudas sobre la aplicación"); // Especifica el asunto del correo
                intent.putExtra(Intent.EXTRA_TEXT, "Hola equipo de soporte de Historia Capital. Deseo que me orienten sobre problemas de mi aplicación... "); // Especifica el texto del cuerpo del correo

                // Iniciar el `Intent` de correo electrónico
                startActivity(intent);
            }
        });

        setupFAQListeners();
    }

    private void setupFAQListeners() {
        for (int i = 1; i <= 11; i++) {
            final int index = i;
            String layoutId = "layout_pregunta_" + i;
            String answerId = "answer" + i;

            int layoutResId = getResources().getIdentifier(layoutId, "id", getPackageName());
            int answerResId = getResources().getIdentifier(answerId, "id", getPackageName());

            final TextView answerTextView = findViewById(answerResId);

            // Configura la justificación para cada answerTextView si es necesario
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                answerTextView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }

            findViewById(layoutResId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleAnswer(answerTextView);
                }
            });

            if (index == 3) {
                String text = "Puedes encontrar los términos y condiciones en nuestro sitio web en la siguiente dirección: www.historiacapital.site/terminos.";
                SpannableString spannableString = new SpannableString(text);
                int start = text.indexOf("www.historiacapital.site/terminos");
                int end = start + "www.historiacapital.site/terminos".length();
                int blueColor = ContextCompat.getColor(this, R.color.blue);
                spannableString.setSpan(new ForegroundColorSpan(blueColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Añadir ClickableSpan para hacer que el texto sea clicable
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        // Crear el `Intent` para abrir el navegador
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.historiacapital.site/terminos"));
                        startActivity(intent);
                    }
                };
                spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                answerTextView.setText(spannableString);
                answerTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if (index == 10) {
                String text = "Puedes contactar a nuestro equipo de soporte vía correo electrónico a contacto@historiacapital.site, indicando tu inquietud o duda. El equipo de soporte estará encantado de ayudarte.";
                SpannableString spannableString = new SpannableString(text);
                int start = text.indexOf("contacto@historiacapital.site");
                int end = start + "contacto@historiacapital.site".length();
                int blueColor = ContextCompat.getColor(this, R.color.blue);
                spannableString.setSpan(new ForegroundColorSpan(blueColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Añadir ClickableSpan para hacer que el texto sea clicable
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        // Crear el `Intent` para enviar un correo
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:contacto@historiacapital.site"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Dudas sobre la aplicación"); // Especifica el asunto del correo
                        intent.putExtra(Intent.EXTRA_TEXT, "Hola equipo de soporte de Historia Capital. Deseo que me orienten sobre problemas de mi aplicación... "); // Especifica el texto del cuerpo del correo
                        startActivity(intent);
                    }
                };
                spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                answerTextView.setText(spannableString);
                answerTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    public void toggleAnswer(View view) {
        // Alternamos la visibilidad de la respuesta
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE); // Si es visible, lo ocultamos
        } else {
            view.setVisibility(View.VISIBLE); // Si está oculto, lo mostramos
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Soporte.this, Homepage.class);
        startActivity(intent);
        finish();
    }
}