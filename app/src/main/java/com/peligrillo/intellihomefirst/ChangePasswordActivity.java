package com.peligrillo.intellihomefirst;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText newPasswordInput;
    private Button changePasswordButton;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        newPasswordInput = findViewById(R.id.newPasswordInput);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        errorText = findViewById(R.id.errorText);

        changePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();

            if (validatePassword(newPassword)) {
                // Aquí enviarías la nueva contraseña al servidor y comprobarías el éxito de la operación
                Toast.makeText(this, "Contraseña cambiada satisfactoriamente", Toast.LENGTH_SHORT).show();

                // Redirigir a la página de inicio de sesión
                Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finalizar ChangePasswordActivity para que no esté en el historial
            }
        });
    }

    private boolean validatePassword(String password) {
        if (password.length() < 7) {
            errorText.setText("La contraseña debe tener al menos 7 caracteres");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            errorText.setText("La contraseña debe contener al menos una letra mayúscula");
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            errorText.setText("La contraseña debe contener al menos una letra minúscula");
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            errorText.setText("La contraseña debe contener al menos un número");
            return false;
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            errorText.setText("La contraseña debe contener al menos un símbolo especial");
            return false;
        }

        // Si la contraseña es válida, borrar cualquier mensaje de error
        errorText.setText("");
        return true;
    }
}
