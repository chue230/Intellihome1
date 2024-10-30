package com.peligrillo.intellihomefirst;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ManageUsersActivity extends AppCompatActivity {

    private ListView usersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        usersListView = findViewById(R.id.usersListView);

        // Iniciar la solicitud para obtener usuarios en un hilo separado
        new Thread(this::obtenerUsuarios).start();
    }

    private void obtenerUsuarios() {
        try (Socket socket = new Socket("172.26.33.246", 1335);
             OutputStreamWriter salida = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

            // Enviar solicitud al servidor
            salida.write("get_users\n");
            salida.flush();

            // Leer la respuesta del servidor
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = entrada.readLine()) != null) {
                responseBuilder.append(line);
            }

            // Convertir la respuesta en JSONArray
            JSONArray usuarios = new JSONArray(responseBuilder.toString());

            // Procesar la lista de usuarios y roles
            ArrayList<String> listaUsuarios = new ArrayList<>();
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject usuario = usuarios.getJSONObject(i);
                String alias = usuario.getString("alias");
                String role = usuario.getString("role");
                listaUsuarios.add(alias + " - " + role);
            }

            // Actualizar el ListView en el hilo de la interfaz
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ManageUsersActivity.this,
                        android.R.layout.simple_list_item_1,
                        listaUsuarios
                );
                usersListView.setAdapter(adapter);
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(
                    ManageUsersActivity.this, "Error al obtener usuarios", Toast.LENGTH_SHORT).show());
        }
    }
}
