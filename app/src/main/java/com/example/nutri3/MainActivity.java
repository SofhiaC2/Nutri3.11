package com.example.nutri3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        TextView btnNavegar = findViewById(R.id.clinscrever); //Botão na primeira tela
        btnNavegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cria um Intent para a segunda tela
                Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
                // Inicia a segunda activity
                startActivity(intent);
            }
        });

    }
}