package com.example.nutri3.fragments.menu; // Ou o pacote correto

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Importe seu ViewBinding se estiver usando
import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentPacienteshostBinding; // Assumindo o nome do seu layout


public class HostPacientesFragment extends Fragment {

    private FragmentPacienteshostBinding binding;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar o layout usando View Binding
        binding = FragmentPacienteshostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Obter o NavController
        navController = NavHostFragment.findNavController(this);

        // 2. Configurar o OnClickListener para o botão "Adicionar Paciente"
        binding.btnMenuAdicionarPaciente.setOnClickListener(v -> {
            // Usa o ID da ACTION que você definiu no nav_main.xml
            navController.navigate(R.id.action_menu_calendar_to_adicionarPacientesFragment);
        });

        // 3. Configurar o OnClickListener para o botão "Ver Pacientes"
        binding.btnMenuVerPaciente.setOnClickListener(v -> {
            // Usa o ID da ACTION correspondente
            navController.navigate(R.id.action_menu_calendar_to_verPacientesFragment);
        });

        // Configure os outros botões ("Atualizar", "Excluir") da mesma forma se eles levarem
        // a outras telas. Se eles executam uma lógica diferente, implemente-a aqui.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpar a referência ao binding
    }
}
