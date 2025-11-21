package com.example.nutri3.fragments.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nutri3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashFragment extends Fragment {

    private static final String TAG = "SplashFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;

            NavController navController = NavHostFragment.findNavController(this);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                Log.d(TAG, "Usuário logado. Trocando para nav_main.");
                NavGraph mainGraph = navController.getNavInflater().inflate(R.navigation.nav_main);
                navController.setGraph(mainGraph, null);
            } else {
                Log.d(TAG, "Usuário não logado. Indo para LoginFragment.");
                navController.navigate(R.id.action_splashFragment_to_loginFragment);
            }
        }, 1500);
    }
}
