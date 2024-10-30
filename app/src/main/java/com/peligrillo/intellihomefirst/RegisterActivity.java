package com.peligrillo.intellihomefirst;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import android.util.Base64;

public class RegisterActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private String generatedCode = "";
    private byte[] imageBytes;  // Bytes de la imagen seleccionada

    private EditText aliasInput, fullNameInput, emailInput, passwordInput, verificationCodeInput;
    private TextView birthDateDisplay, resultText;
    private Button selectBirthDateButton, uploadPhotoButton, validateEmailButton, completeRegistrationButton;
    private CheckBox styleModern, styleClassic, styleMinimalist, transportCar, transportMotorcycle, transportBicycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicialización de los componentes de la interfaz
        aliasInput = findViewById(R.id.aliasRegister);
        fullNameInput = findViewById(R.id.fullName);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        verificationCodeInput = findViewById(R.id.verificationCode);
        birthDateDisplay = findViewById(R.id.birthDateDisplay);


        selectBirthDateButton = findViewById(R.id.selectBirthDateButton);
        uploadPhotoButton = findViewById(R.id.uploadProfilePhoto);
        validateEmailButton = findViewById(R.id.validateEmailButton);
        completeRegistrationButton = findViewById(R.id.completeRegistrationButton);

        styleModern = findViewById(R.id.styleModern);
        styleClassic = findViewById(R.id.styleClassic);
        styleMinimalist = findViewById(R.id.styleMinimalist);

        transportCar = findViewById(R.id.transportCar);
        transportMotorcycle = findViewById(R.id.transportMotorcycle);
        transportBicycle = findViewById(R.id.transportBicycle);

        // Configuración de los listeners
        selectBirthDateButton.setOnClickListener(v -> showDatePickerDialog());
        uploadPhotoButton.setOnClickListener(v -> selectImage());

        validateEmailButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                generatedCode = generateCode();
                sendEmailToServer(email, generatedCode);
            } else {
                Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show();  // Mostrar error en un Toast
            }
        });

        completeRegistrationButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();
            String enteredCode = verificationCodeInput.getText().toString().trim();

            if (enteredCode.equals(generatedCode)) {
                if (validatePassword(password)) {
                    checkUserBeforeRegistration();  // Enviar datos solo si la contraseña es válida
                }
            } else {
                resultText.setText("Código incorrecto");
            }
        });
    }
    // verificacion de correo
    private void checkUserBeforeRegistration() {
        new Thread(() -> {
            try (Socket socket = new Socket("172.26.42.125", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Crear JSON con los datos a verificar
                JSONObject message = new JSONObject();
                message.put("action", "check_user");
                message.put("alias", aliasInput.getText().toString().trim());
                message.put("email", emailInput.getText().toString().trim());

                // Enviar la solicitud al servidor
                output.write(message.toString() + "\n");
                output.flush();

                // Leer la respuesta del servidor
                String response = input.readLine();
                JSONObject jsonResponse = new JSONObject(response);

                runOnUiThread(() -> {
                    try {
                        if ("available".equals(jsonResponse.getString("status"))) {
                            // Si el usuario es válido, proceder al registro
                            sendRegistrationDataToServer();
                        } else {
                            Toast.makeText(this, "Alias o correo ya registrados", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error procesando respuesta", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show());
            }
        }).start();
    }



    //verificación de contraseña

    private boolean validatePassword(String password) {
        if (password.length() < 7) {
            Toast.makeText(this, "La contraseña debe tener al menos 7 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            Toast.makeText(this, "La contraseña debe tener al menos una mayúscula", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            Toast.makeText(this, "La contraseña debe tener al menos una minúscula", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            Toast.makeText(this, "La contraseña debe tener al menos un número", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            Toast.makeText(this, "La contraseña debe tener al menos un símbolo especial", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendRegistrationDataToServer() {
        new Thread(() -> {
            try (Socket socket = new Socket("172.26.42.125", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
                 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Crear JSON con los datos del usuario
                JSONObject message = new JSONObject();
                message.put("action", "register");
                message.put("alias", aliasInput.getText().toString().trim());
                message.put("fullname", fullNameInput.getText().toString().trim());
                message.put("email", emailInput.getText().toString().trim());
                message.put("password", passwordInput.getText().toString().trim());
                message.put("birthdate", birthDateDisplay.getText().toString().trim());
                message.put("role", "usuario");
                message.put("styles", getSelectedHouseStyles().toString());
                message.put("transports", getSelectedTransports().toString());

                // Enviar el JSON al servidor
                output.write(message.toString() + "\n");
                output.flush();

                // Enviar la longitud de la imagen como encabezado (16 bytes)
                if (imageBytes != null) {
                    String imageSize = String.format("%016d", imageBytes.length);
                    socket.getOutputStream().write(imageSize.getBytes());
                    socket.getOutputStream().flush();

                    // Enviar los bytes de la imagen
                    socket.getOutputStream().write(imageBytes);
                    socket.getOutputStream().flush();
                }

                // Leer la respuesta del servidor
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = input.readLine()) != null) {
                    responseBuilder.append(line);
                }

                // Procesar la respuesta del servidor
                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                runOnUiThread(() -> {
                    try {
                        if ("registered".equals(jsonResponse.getString("status"))) {
                            Toast.makeText(this, "Registro completado", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error procesando respuesta", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }


    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    birthDateDisplay.setText(date);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                imageBytes = byteArrayOutputStream.toByteArray();
                Toast.makeText(this, "Imagen seleccionada correctamente", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al seleccionar imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String generateCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private List<String> getSelectedHouseStyles() {
        List<String> styles = new ArrayList<>();
        if (styleModern.isChecked()) styles.add("Moderno");
        if (styleClassic.isChecked()) styles.add("Clásico");
        if (styleMinimalist.isChecked()) styles.add("Minimalista");
        return styles;
    }

    private List<String> getSelectedTransports() {
        List<String> transports = new ArrayList<>();
        if (transportCar.isChecked()) transports.add("Automóvil");
        if (transportMotorcycle.isChecked()) transports.add("Motocicleta");
        if (transportBicycle.isChecked()) transports.add("Bicicleta");
        return transports;
    }

    private void sendEmailToServer(String email, String code) {
        new Thread(() -> {
            try (Socket socket = new Socket("172.26.42.125", 1335);
                 OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream())) {

                JSONObject message = new JSONObject();
                message.put("action", "send_code");
                message.put("email", email);
                message.put("code", code);

                output.write(message.toString() + "\n");
                output.flush();

                runOnUiThread(() -> Toast.makeText(this, "Correo enviado con éxito", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
