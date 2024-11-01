package com.peligrillo.intellihomefirst;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;  // Asegúrate de importar la clase correcta
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText editTextCode;
    private TextView textViewTimer;
    private Button buttonVerifyCode;
    private Handler handler = new Handler();
    private String email;
    private int remainingTime = 120;  // Tiempo predeterminado en segundos (2 minutos)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        editTextCode = findViewById(R.id.editTextCode);
        textViewTimer = findViewById(R.id.textViewTimer);
        buttonVerifyCode = findViewById(R.id.buttonVerifyCode);

        // Obtener el correo desde el Intent
        email = getIntent().getStringExtra("email");

        // Empezar a obtener el tiempo restante
        startTimer();

        buttonVerifyCode.setOnClickListener(v -> verifyCode());
    }

    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getRemainingTimeFromServer();
                handler.postDelayed(this, 1000); // Actualizar cada segundo
            }
        }, 0);
    }

    private void getRemainingTimeFromServer() {
        new Thread(() -> {
            try (Socket socket = new Socket("172.26.42.125", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Crear solicitud JSON para obtener el tiempo restante
                JSONObject request = new JSONObject();
                request.put("action", "get_remaining_time");
                request.put("email", email);

                output.write(request.toString() + "\n");
                output.flush();

                // Leer la respuesta del servidor
                String response = input.readLine();
                JSONObject jsonResponse = new JSONObject(response);

                // Extraer los valores antes de la expresión lambda
                String status = jsonResponse.getString("status");
                int remainingTimeFromServer = jsonResponse.optInt("remaining_time", -1); // -1 si no está presente

                runOnUiThread(() -> {
                    if ("time_remaining".equals(status)) {
                        remainingTime = remainingTimeFromServer;
                        updateTimerText();
                    } else if ("expired".equals(status)) {
                        Toast.makeText(this, "El código ha expirado", Toast.LENGTH_SHORT).show();
                        handler.removeCallbacksAndMessages(null); // Detener el temporizador
                        finish(); // Volver a la pantalla principal
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al obtener el tiempo", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateTimerText() {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        textViewTimer.setText(String.format("Tiempo restante: %02d:%02d", minutes, seconds));
    }

    private void verifyCode() {
        String code = editTextCode.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(this, "Ingresa el código", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try (Socket socket = new Socket("172.26.42.125", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Crear solicitud JSON para verificar el código
                JSONObject request = new JSONObject();
                request.put("action", "verify_code");
                request.put("email", email);
                request.put("code", code);

                output.write(request.toString() + "\n");
                output.flush();

                // Leer la respuesta del servidor
                String response = input.readLine();
                JSONObject jsonResponse = new JSONObject(response);

                // Extraer los valores antes de la expresión lambda
                String status = jsonResponse.getString("status");

                runOnUiThread(() -> {
                    if ("code_verified".equals(status)) {
                        Toast.makeText(this, "Código verificado", Toast.LENGTH_SHORT).show();
                        // Ir a la pantalla de cambio de contraseña
                        Intent intent = new Intent(VerifyCodeActivity.this, ChangePasswordActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Código incorrecto o expirado", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al verificar el código", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);  // Detener el temporizador cuando se destruya la actividad
    }
}
