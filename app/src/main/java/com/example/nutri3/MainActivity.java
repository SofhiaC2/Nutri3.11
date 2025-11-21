package com.example.nutri3;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.nutri3.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            Log.e(TAG, "NavHostFragment não encontrado! Verifique o ID no activity_main.xml.");
            finish();
            return;
        }

        navController = navHostFragment.getNavController();
        mAuth = FirebaseAuth.getInstance();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.menu_home, R.id.hostPacientesFragment2, R.id.menu_config
        ).build();

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        setupAuthStateListener();
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                if (navController.getGraph().getId() != R.id.nav_main) {
                    try {
                        navController.setGraph(R.navigation.nav_main);
                        Log.d(TAG, "AuthState: Usuário LOGADO. Garantindo nav_main.");
                    } catch (Exception e) {
                        Log.e(TAG, "Falha ao definir nav_main no listener.", e);
                    }
                }
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            } else {
                if (navController.getGraph().getId() != R.id.nav_login) {
                    try {
                        navController.setGraph(R.navigation.nav_login);
                        Log.d(TAG, "AuthState: Usuário NÃO LOGADO. Trocando para nav_login.");
                    } catch (Exception e) {
                        Log.e(TAG, "Falha ao definir nav_login no listener.", e);
                    }
                }
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
