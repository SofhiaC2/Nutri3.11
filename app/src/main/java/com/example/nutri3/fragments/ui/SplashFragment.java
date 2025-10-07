package com.example.nutri3.fragments.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // Adicionar para logs
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.nutri3.R;

public class SplashFragment extends Fragment {
    private static final String TAG = "SplashFragment"; // Tag para logs
    private static final int SPLASH_TIMEOUT = 2000;

    public SplashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Handler executando após timeout.");
            // Verifica se o fragmento ainda está adicionado à atividade
            // e se o NavController ainda está no contexto do nav_login
            // antes de tentar navegar.
            if (isAdded() && getActivity() != null) {
                try {
                    NavController navController = NavHostFragment.findNavController(SplashFragment.this);
                    // Só navega para login SE o gráfico atual AINDA FOR o nav_login.
                    // Se a MainActivity já trocou para nav_main, esta condição será falsa.
                    if (navController.getGraph().getId() == R.id.nav_login) {
                        Log.d(TAG, "Navegando para LoginFragment a partir do Splash.");
                        navController.navigate(R.id.action_splashFragment_to_loginFragment);
                    } else {
                        Log.d(TAG, "Não navegou para Login, gráfico atual não é nav_login (provavelmente nav_main). ID do gráfico atual: " + navController.getGraph().getId());
                    }
                } catch (IllegalStateException e) {
                    // Isso pode acontecer se o NavController não estiver mais disponível ou o fragmento desanexado
                    Log.e(TAG, "IllegalStateException ao tentar navegar: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    // Isso pode acontecer se a ação não for encontrada (ex: se o gráfico já mudou para nav_main)
                    Log.e(TAG, "IllegalArgumentException ao tentar navegar (ação não encontrada?): " + e.getMessage());
                }
            } else {
                Log.d(TAG, "SplashFragment não está mais adicionado ou Activity é nula. Não vai navegar.");
            }
        }, SPLASH_TIMEOUT);
    }
}
