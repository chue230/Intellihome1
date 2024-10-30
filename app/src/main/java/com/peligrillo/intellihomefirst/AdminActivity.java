package com.peligrillo.intellihomefirst;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private TextView adminGreeting;
    private Button logoutButton, manageUsersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        adminGreeting = findViewById(R.id.adminGreeting);
        logoutButton = findViewById(R.id.logoutButton);
        manageUsersButton = findViewById(R.id.manageUsersButton);

        // Obtener el alias pasado desde MainActivity
        String alias = getIntent().getStringExtra("alias");

        // Mostrar el saludo con el alias del administrador
        adminGreeting.setText("Hola Administrador " + alias);

        // Configurar el botón de cerrar sesión
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Configurar el botón "Administrar Usuarios"
        manageUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });
    }
}
