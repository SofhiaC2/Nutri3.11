package com.example.nutri3.fragments.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentConfigBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ConfigFragment extends Fragment {

    private FragmentConfigBinding binding;
    private FirebaseAuth mAuth;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {

        binding.cardEditarPerfil.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("isEditMode", true);
            NavHostFragment.findNavController(this).navigate(R.id.action_menu_config_to_registerFragment2, args);
        });

        binding.cardSair.setOnClickListener(v -> {
            mostrarDialogoSair();
        });

        binding.cardSobreApp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Nutri3 App v1.0\nDesenvolvido por Sofhia", Toast.LENGTH_LONG).show();
        });



        binding.cardSair.setOnClickListener(v -> {
            mostrarDialogoSair();
        });

        binding.cardSobreApp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Nutri3 App v1.0\nDesenvolvido por Sofhia", Toast.LENGTH_LONG).show();
        });
    }


    private void mostrarDialogoSair() {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Sair da Conta")
                .setMessage("Você tem certeza que deseja sair?")
                .setPositiveButton("Sair", (dialog, which) -> {
                    fazerLogout();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void fazerLogout() {
        mAuth.signOut();

        if (getActivity() != null) {

            Intent intent = new Intent(getActivity(), com.example.nutri3.MainActivity.class);


            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            getActivity().finish();
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
