package com.peligrillo.intellihomefirst;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private EditText aliasInput, passwordInput;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aliasInput = findViewById(R.id.aliasInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String alias = aliasInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (alias.isEmpty() || password.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "Por favor, introduce alias y contraseña.", Toast.LENGTH_SHORT).show());
            return;
        }

        // Ejecutar la conexión al servidor en un hilo separado
        new Thread(() -> {
            try (Socket socket = new Socket("172.26.42.125", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Crear la solicitud en formato JSON
                JSONObject request = new JSONObject();
                request.put("action", "login");
                request.put("alias", alias);
                request.put("password", password);

                // Enviar la solicitud al servidor
                output.write(request.toString() + "\n");  // Importante: Fin de línea
                output.flush();

                // Leer la respuesta completa del servidor
                String response = input.readLine();  // Leer la línea completa
                JSONObject jsonResponse = new JSONObject(response);

                runOnUiThread(() -> {
                    try {
                        if ("success".equals(jsonResponse.getString("status"))) {
                            String role = jsonResponse.getString("role");
                            if ("admin".equals(role)) {
                                openAdminActivity(alias);
                            } else {
                                openUserActivity(alias);
                            }
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Alias o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private void openAdminActivity(String alias) {
        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
        intent.putExtra("alias", alias);
        startActivity(intent);
        finish();
    }

    private void openUserActivity(String alias) {
        Intent intent = new Intent(MainActivity.this, UserActivity.class);
        intent.putExtra("alias", alias);
        startActivity(intent);
        finish();
    }
}
