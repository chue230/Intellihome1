package com.peligrillo.intellihomefirst;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button sendEmailButton;
    private TextView errorText;
    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.emailInput);
        sendEmailButton = findViewById(R.id.sendEmailButton);
        errorText = findViewById(R.id.errorText);

        sendEmailButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                checkEmailAndSendCode(email);
            } else {
                errorText.setText("Por favor, ingrese un correo electrónico válido");
            }
        });
    }

    private void checkEmailAndSendCode(String email) {
        new Thread(() -> {
            try (Socket socket = new Socket("tu.direccion.ip", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Crear solicitud JSON para verificar el correo
                JSONObject request = new JSONObject();
                request.put("action", "check_user_email");
                request.put("email", email);

                // Enviar solicitud al servidor
                output.write(request.toString() + "\n");
                output.flush();

                // Leer la respuesta del servidor
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = input.readLine()) != null) {
                    responseBuilder.append(line);
                }

                JSONObject response = new JSONObject(responseBuilder.toString());
                if ("exists".equals(response.getString("status"))) {
                    runOnUiThread(() -> sendVerificationCode(email));
                } else {
                    runOnUiThread(() -> errorText.setText("No se encontró una cuenta con este correo."));
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendVerificationCode(String email) {
        verificationCode = generateVerificationCode();

        new Thread(() -> {
            try (Socket socket = new Socket("tu.direccion.ip", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream())) {

                // Crear solicitud JSON para enviar el código al correo
                JSONObject request = new JSONObject();
                request.put("action", "send_code");
                request.put("email", email);
                request.put("code", verificationCode);

                // Enviar solicitud al servidor
                output.write(request.toString() + "\n");
                output.flush();

                runOnUiThread(() -> {
                    Toast.makeText(ForgotPasswordActivity.this, "Código enviado al correo", Toast.LENGTH_SHORT).show();
                    // Redirigir a la Activity de verificación de código
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                    intent.putExtra("email", email);  // Pasar el correo para la siguiente Activity
                    intent.putExtra("verificationCode", verificationCode);  // Pasar el código generado
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Error enviando el código", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%05d", random.nextInt(100000));  // Código de 5 dígitos
    }
}
