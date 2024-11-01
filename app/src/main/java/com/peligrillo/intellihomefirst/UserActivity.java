package com.peligrillo.intellihomefirst;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        TextView aliasView = findViewById(R.id.aliasView);

        String alias = getIntent().getStringExtra("alias");
        aliasView.setText("Bienvenido, " + alias);
    }
}
