package com.example.nutri3;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); // deve ter o nav_ac_login

        // Obtém o NavHostFragment do layout
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_ac_login);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // Aqui você pode usar navController.navigate() se precisar
        }
    }
}
