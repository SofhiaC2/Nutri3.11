package com.example.nutri3.fragments.menu;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentPacienteshostBinding;

public class HostPacientesFragment extends Fragment {

    private FragmentPacienteshostBinding binding;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPacienteshostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        // AJUSTE: Configurar botão de voltar
        binding.btnPacientesVoltar.setOnClickListener(v -> {
            // Este comando é mais seguro, pois ele sobe na pilha de navegação
            // ou finaliza a activity se não houver para onde voltar.
            // Isso o levará de volta para a tela anterior (ex: Home).
            requireActivity().onBackPressed();
        });

        // AJUSTE: Usar os novos IDs de ação definidos em nav_pacientes.xml
        binding.btnMenuAdicionarPaciente.setOnClickListener(v -> {
            navController.navigate(R.id.action_hostPacientes_to_adicionarPacientes);
        });

        binding.btnMenuVerPaciente.setOnClickListener(v -> {
            navController.navigate(R.id.action_hostPacientes_to_verPacientes);
        });

        // Você pode configurar os outros botões aqui quando criar as telas para eles.
        // Por exemplo:
        // binding.btnMenuAtualizarPaciente.setOnClickListener(v -> navController.navigate(R.id.sua_acao_para_atualizar));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
