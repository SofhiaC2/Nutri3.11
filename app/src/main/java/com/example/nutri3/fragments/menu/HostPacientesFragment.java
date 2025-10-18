package com.example.nutri3.fragments.menu;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Import do AlertDialog
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Import do ViewModelProvider
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox; // Import do CheckBox
import android.widget.Toast;   // Import do Toast

import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentPacienteshostBinding;
import com.example.nutri3.ViewModel.ConsultaViewModel; // Import do nosso ViewModel

public class HostPacientesFragment extends Fragment {

    private FragmentPacienteshostBinding binding;
    private NavController navController;

    // Acessa o ViewModel compartilhado
    private ConsultaViewModel consultaViewModel;

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

        // Inicializa o ViewModel no escopo da Activity (para ser compartilhado)
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnPacientesVoltar.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        binding.btnMenuAdicionarPaciente.setOnClickListener(v -> {
            navController.navigate(R.id.action_hostPacientes_to_adicionarPacientes);
        });

        binding.btnMenuVerPaciente.setOnClickListener(v -> {
            navController.navigate(R.id.action_hostPacientes_to_verPacientes);
        });

        // --- LÓGICA DO BOTÃO "NOVA CONSULTA" ---
        binding.btnMenuNovaConsulta.setOnClickListener(v -> {
            mostrarDialogoNovaConsulta();
        });

        // Você pode configurar os outros botões aqui quando criar as telas para eles.
    }

    // --- FUNÇÃO PARA MOSTRAR O DIÁLOGO DE SELEÇÃO ---
    private void mostrarDialogoNovaConsulta() {
        if (getContext() == null) return;

        // Limpa os dados do ViewModel de uma consulta anterior antes de começar uma nova
        consultaViewModel.limparDados();

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialogo_nova_consulta, null);

        final CheckBox cbAvaliacao = dialogView.findViewById(R.id.cbAvaliacao);
        final CheckBox cbDieta = dialogView.findViewById(R.id.cbDieta);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Nova Consulta")
                .setPositiveButton("Avançar", (dialog, which) -> {
                    boolean avaliacaoChecked = cbAvaliacao.isChecked();
                    boolean dietaChecked = cbDieta.isChecked();

                    // Salva as escolhas no ViewModel
                    consultaViewModel.setOpcoesConsulta(avaliacaoChecked, dietaChecked);

                    if (!avaliacaoChecked && !dietaChecked) {
                        Toast.makeText(getContext(), "Selecione ao menos uma opção.", Toast.LENGTH_SHORT).show();
                        return; // Não faz nada se nada for selecionado
                    }

                    // Navega para a primeira tela do fluxo
                    if (avaliacaoChecked) {
                        // Se "Avaliação" foi marcada, SEMPRE vai para ela primeiro.
                        navController.navigate(R.id.action_hostPacientesFragment_to_avaliacaoFragment);
                    } else {
                        // Se apenas "Dieta" foi marcada, vai direto para ela.
                        navController.navigate(R.id.action_hostPacientesFragment_to_dietaFragment);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
