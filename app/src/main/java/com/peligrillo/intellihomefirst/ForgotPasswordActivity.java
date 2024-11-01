package com.peligrillo.intellihomefirst;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button sendCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.emailInput);
        sendCodeButton = findViewById(R.id.sendCodeButton);

        sendCodeButton.setOnClickListener(v -> sendEmail());
    }

    private void sendEmail() {
        String email = emailInput.getText().toString().trim();

        new Thread(() -> {
            try (Socket socket = new Socket("172.26.42.125", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                JSONObject request = new JSONObject();
                request.put("action", "forgot_password");
                request.put("email", email);

                output.write(request.toString() + "\n");
                output.flush();

                String response = input.readLine();
                JSONObject jsonResponse = new JSONObject(response);

                runOnUiThread(() -> {
                    if (jsonResponse.getString("status").equals("code_sent")) {
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Error al enviar código", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
