
package com.example.nutri3.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.nutri3.R;
// Importe o ViewBinding para o seu layout da dieta
import com.example.nutri3.databinding.FragmentDietaBinding;
// Importe o ViewModel do pacote correto
import com.example.nutri3.ViewModel.ConsultaViewModel;

public class DietaFragment extends Fragment {

    // Declarações com ViewBinding e para a lógica de navegação/dados
    private FragmentDietaBinding binding;
    private NavController navController;
    private ConsultaViewModel consultaViewModel;

    public DietaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Usa o ViewBinding para inflar o layout
        binding = FragmentDietaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa o NavController e o ViewModel (mesma instância da Activity)
        navController = NavHostFragment.findNavController(this);
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        // Configura os cliques dos botões
        setupClickListeners();

        // Opcional: Acessa os dados da avaliação para usar nesta tela
        observeViewModel();
    }

    private void observeViewModel() {
        // Exemplo de como usar os dados da avaliação que vieram da tela anterior
        consultaViewModel.getAvaliacao().observe(getViewLifecycleOwner(), avaliacao -> {
            if (avaliacao != null && avaliacao.getAltura() > 0) {
                // Calcula o IMC apenas para mostrar que os dados estão acessíveis
                double imc = avaliacao.getPeso() / (avaliacao.getAltura() * avaliacao.getAltura());
                String imcFormatado = String.format("%.2f", imc);
                Toast.makeText(getContext(), "Dados da avaliação recebidos! IMC calculado: " + imcFormatado, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // Botão para voltar à tela anterior (Avaliação Física, se aplicável)
        binding.btnVoltar.setOnClickListener(v -> {
            // TODO: Salvar os dados da dieta no ViewModel antes de voltar, se necessário.
            navController.popBackStack();
        });

        // Botão para finalizar e salvar toda a consulta
        // NOTE: Seu XML não tem um botão de "Salvar Consulta". Adicionei a lógica ao botão do café da manhã como exemplo.
        // Você deve criar um botão "Salvar Consulta" no seu XML e atrelar esta lógica a ele.
        binding.btnAddCafeManha.setOnClickListener(v -> {
            // TODO: Implementar a lógica de adicionar um alimento ao café da manhã.
            // Esta é uma placeholder para o botão de salvar final.
            finalizarConsulta();
        });

        // Adicione aqui os listeners para os outros botões "+ Adicionar alimento"
        binding.btnAddAlmoco.setOnClickListener(v -> Toast.makeText(getContext(), "Adicionar Almoço: A implementar.", Toast.LENGTH_SHORT).show());
        binding.btnAddCafeTarde.setOnClickListener(v -> Toast.makeText(getContext(), "Adicionar Café da Tarde: A implementar.", Toast.LENGTH_SHORT).show());
        binding.btnAddJantar.setOnClickListener(v -> Toast.makeText(getContext(), "Adicionar Jantar: A implementar.", Toast.LENGTH_SHORT).show());
        binding.btnAddCeia.setOnClickListener(v -> Toast.makeText(getContext(), "Adicionar Ceia: A implementar.", Toast.LENGTH_SHORT).show());
    }

    private void finalizarConsulta() {
        // TODO: Aqui você pegaria os dados da Dieta e da Avaliação do ViewModel
        // e os salvaria permanentemente no Firebase ou banco de dados local.

        Toast.makeText(getContext(), "Consulta completa salva com sucesso!", Toast.LENGTH_LONG).show();

        // Após salvar, limpa o ViewModel para a próxima consulta não ter dados antigos
        consultaViewModel.limparDados();

        // Navega de volta para a tela inicial dos pacientes, limpando a pilha de navegação.
        navController.popBackStack(R.id.hostPacientesFragment, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpa a referência ao binding
    }
}
