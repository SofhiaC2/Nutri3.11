package com.example.nutri3;

import android.os.Bundle;
import android.util.Log;
import android.view.View; // Importar View

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration; // Importar
import androidx.navigation.ui.NavigationUI;       // Importar

import com.example.nutri3.databinding.ActivityMainBinding; // Vamos usar View Binding aqui também!
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding; // Usar View Binding para segurança e clareza
    private NavController navController;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuração com View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Encontra o NavHostFragment (é o container dos seus fragmentos)
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_ac_login); // Confirme que este ID está no seu activity_main.xml

        if (navHostFragment == null) {
            Log.e(TAG, "NavHostFragment não encontrado!");
            finish();
            return;
        }
        navController = navHostFragment.getNavController();
        mAuth = FirebaseAuth.getInstance();

        // **A PARTE MAIS IMPORTANTE DA CORREÇÃO**
        // Define quais são os destinos de "nível superior". A BottomNavigationView
        // só aparecerá nestes fragmentos.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.menu_home, R.id.menu_consults, R.id.menu_calendar, R.id.menu_config
        ).build();

        // Conecta a BottomNavigationView ao NavController.
        // Isto faz com que clicar nos ícones troque os fragmentos E gerencie a visibilidade.
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController); // Use o ID do seu BottomNavigationView

        // Listener para gerenciar a troca entre os grafos de login e principal
        setupAuthStateListener();
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // USUÁRIO LOGADO
                // Se não estiver já no grafo principal, troca para ele.
                if (navController.getGraph().getId() != R.id.nav_main) {
                    navController.setGraph(R.navigation.nav_main);
                    Log.d(TAG, "AuthState: Usuário LOGADO. Trocando para nav_main.");
                }
                // Mostra a BottomNavigationView
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            } else {
                // USUÁRIO NÃO LOGADO
                // Se não estiver já no grafo de login, troca para ele.
                if (navController.getGraph().getId() != R.id.nav_login) {
                    navController.setGraph(R.navigation.nav_login);
                    Log.d(TAG, "AuthState: Usuário NÃO LOGADO. Trocando para nav_login.");
                }
                // Esconde a BottomNavigationView
                binding.bottomNavigation.setVisibility(View.GONE);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
}
