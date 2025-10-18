package com.example.nutri3.fragments.menu; // Ajuste o pacote se necessário

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
            NavHostFragment.findNavController(this).navigate(R.id.registerFragment, args);
        });

        binding.cardSair.setOnClickListener(v -> {
            mostrarDialogoSair();
        });

        // --- PARTE QUE FALTAVA ---
        // Adiciona a funcionalidade de clique para o card "Sobre o App"
        binding.cardSobreApp.setOnClickListener(v -> {
            // Mostra uma mensagem simples. No futuro, você pode substituir isso
            // por um diálogo mais elaborado com informações do app.
            Toast.makeText(getContext(), "Nutri3 App v1.0\nDesenvolvido por [Seu Nome]", Toast.LENGTH_LONG).show();
        });
        // --- FIM DA PARTE QUE FALTAVA ---
    }

    private void mostrarDialogoSair() {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Sair da Conta")
                .setMessage("Você tem certeza que deseja sair?")
                .setPositiveButton("Sair", (dialog, which) -> {
                    fazerLogout(); // Apenas chama o método
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void fazerLogout() {
        // --- INÍCIO DA ALTERAÇÃO CORRETA ---
        // Apenas desconecte o usuário.
        // O AuthStateListener na sua MainActivity fará o resto (trocar para nav_login).
        mAuth.signOut();
        // Não é necessário criar um Intent ou finalizar a activity.
        // ---- FIM DA ALTERAÇÃO CORRETA ----
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpa a referência ao binding
    }
}
