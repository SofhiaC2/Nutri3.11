package com.example.nutri3.fragments;

import android.os.Bundle;
import android.util.Log;
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
import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.databinding.FragmentDietaBinding;
import com.example.nutri3.model.Avaliacao;
import com.example.nutri3.model.Dieta; // Certifique-se que você tem este model
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DietaFragment extends Fragment {

    private static final String TAG = "DietaFragment";
    private FragmentDietaBinding binding;
    private NavController navController;
    private ConsultaViewModel consultaViewModel;
    private String pacienteId;

    public DietaFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDietaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        // Pega o ID do paciente do ViewModel
        consultaViewModel.getPacienteIdSelecionado().observe(getViewLifecycleOwner(), id -> {
            if (id != null) {
                this.pacienteId = id;
            }
        });

        // Observa e preenche os dados da dieta, se existirem (para quando o usuário volta da tela de adicionar alimentos)
        consultaViewModel.getDadosDieta().observe(getViewLifecycleOwner(), dieta -> {
            if (dieta != null) {
                // Aqui você implementará a lógica para preencher os RecyclerViews com os dados da dieta
            }
        });

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnVoltar.setOnClickListener(v -> {
            // Salva o estado atual da dieta antes de voltar para a avaliação
            salvarDadosDietaNoViewModel();
            navController.popBackStack();
        });

        // Botão para finalizar e salvar toda a consulta
        binding.btnSalvar.setOnClickListener(v -> {
            salvarDadosDietaNoViewModel();
            finalizarESalvarConsulta();
        });

        // Listeners para adicionar alimentos (a implementar)
        binding.btnAddCafeManha.setOnClickListener(v -> Toast.makeText(getContext(), "Navegar para busca de alimentos...", Toast.LENGTH_SHORT).show());
        binding.btnAddAlmoco.setOnClickListener(v -> Toast.makeText(getContext(), "Navegar para busca de alimentos...", Toast.LENGTH_SHORT).show());
        // etc...
    }

    private void salvarDadosDietaNoViewModel() {
        // Crie seu modelo de Dieta para organizar os alimentos.
        // Por agora, vamos criar um objeto vazio apenas para o fluxo funcionar.
        Dieta dietaAtual = new Dieta();
        // Ex: dietaAtual.setCafeDaManha(alimentosDoCafe);
        // ... preencha com todos os dados dos RecyclerViews ...

        consultaViewModel.atualizarDadosDieta(dietaAtual);
        Log.d(TAG, "Dados da dieta (placeholder) salvos no ViewModel.");
    }

    private void finalizarESalvarConsulta() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || pacienteId == null) {
            Toast.makeText(getContext(), "Erro: Usuário ou paciente não identificado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pega os dados finais do ViewModel
        Avaliacao avaliacao = consultaViewModel.getDadosAvaliacao().getValue();
        Dieta dieta = consultaViewModel.getDadosDieta().getValue();

        // Se o nutricionista não preencheu nada na avaliação ou na dieta, não salva.
        // A lógica de "se ele não quiser usar" é tratada aqui.
        if (avaliacao == null && dieta == null) {
            Toast.makeText(getContext(), "Nenhum dado de avaliação ou dieta foi preenchido para salvar.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSalvar.setEnabled(false); // Desabilita o botão para evitar cliques duplos

        // Cria as tarefas para salvar no Firebase
        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference("pacientes");

        List<Task<Void>> tasks = new ArrayList<>();

        // Salva a avaliação se ela não for nula
        if (avaliacao != null) {
            DatabaseReference avaliacaoRef = baseRef.child(currentUser.getUid()).child(pacienteId).child("avaliacoes").push();
            tasks.add(avaliacaoRef.setValue(avaliacao));
        }

        // Salva a dieta se ela não for nula
        if (dieta != null) {
            DatabaseReference dietaRef = baseRef.child(currentUser.getUid()).child(pacienteId).child("dietas").push();
            tasks.add(dietaRef.setValue(dieta));
        }

        // Executa todas as tarefas de salvamento em paralelo
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(list -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Consulta salva com sucesso!", Toast.LENGTH_LONG).show();
            consultaViewModel.limparDados(); // Limpa o ViewModel para a próxima consulta
            // Navega de volta para a tela inicial dos pacientes
            navController.popBackStack(R.id.hostPacientesFragment2, false);

        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSalvar.setEnabled(true); // Reabilita o botão em caso de falha
            Toast.makeText(getContext(), "Falha ao salvar a consulta.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erro ao salvar consulta no Firebase", e);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Salva o estado da dieta sempre que sair da tela
        salvarDadosDietaNoViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
