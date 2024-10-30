package com.peligrillo.intellihomefirst;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText codeInput;
    private Button verifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_code_activity);

        codeInput = findViewById(R.id.codeInput);
        verifyButton = findViewById(R.id.verifyButton);

        verifyButton.setOnClickListener(v -> verifyCode());
    }

    private void verifyCode() {
        String code = codeInput.getText().toString().trim();

        new Thread(() -> {
            try (Socket socket = new Socket("172.26.42.125", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                JSONObject request = new JSONObject();
                request.put("action", "verify_code");
                request.put("code", code);

                output.write(request.toString() + "\n");
                output.flush();

                String response = input.readLine();
                JSONObject jsonResponse = new JSONObject(response);

                runOnUiThread(() -> {
                    if (jsonResponse.getString("status").equals("code_valid")) {
                        Intent intent = new Intent(VerifyCodeActivity.this, ChangePasswordActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Código incorrecto o expirado", Toast.LENGTH_SHORT).show();
                        finish();  // Volver a la ventana principal
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
