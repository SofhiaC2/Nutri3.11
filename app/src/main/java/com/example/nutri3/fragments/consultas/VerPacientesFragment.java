package com.example.nutri3.fragments.consultas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.nutri3.fragments.consultas.VerPacientesFragmentDirections;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Import necessário para o diálogo
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.nutri3.fragments.consultas.Paciente;

import com.example.nutri3.adapters.PacienteAdapter;
import com.example.nutri3.databinding.FragmentVerPacientesBinding;
import com.example.nutri3.fragments.consultas.Paciente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// --- MUDANÇA 1: Implementar a interface do Adapter ---
public class VerPacientesFragment extends Fragment implements PacienteAdapter.OnPacienteInteractionListener {

    private FragmentVerPacientesBinding binding;
    private NavController navController;
    private PacienteAdapter adapter;
    private final List<Paciente> listaPacientes = new ArrayList<>();
    // Listener do Firebase para poder removê-lo no onDestroy e evitar memory leaks
    private ValueEventListener pacientesValueEventListener;
    private DatabaseReference userPacientesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVerPacientesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        setupToolbar();
        setupRecyclerView();
        setupSearch();

        carregarPacientesDoFirebase();
    }

    private void setupToolbar() {
        binding.btnVerPacientesVoltar.setOnClickListener(v -> navController.popBackStack());
    }

    private void setupRecyclerView() {
        binding.recyclerViewPacientes.setLayoutManager(new LinearLayoutManager(getContext()));
        // O adapter será criado e definido quando os dados chegarem
    }

    private void carregarPacientesDoFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showEmptyState("Faça o login para ver seus pacientes.");
            return;
        }

        binding.progressBarVerPacientes.setVisibility(View.VISIBLE);
        binding.recyclerViewPacientes.setVisibility(View.GONE);
        binding.tvListaVazia.setVisibility(View.GONE);

        String userId = currentUser.getUid();
        userPacientesRef = FirebaseDatabase.getInstance().getReference("pacientes").child(userId);

        // Previne a criação de múltiplos listeners
        if (pacientesValueEventListener != null) {
            userPacientesRef.removeEventListener(pacientesValueEventListener);
        }

        pacientesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return; // Previne crash se o fragmento for fechado

                listaPacientes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Paciente paciente = snapshot.getValue(Paciente.class);
                    if (paciente != null) {
                        paciente.setId(snapshot.getKey()); // Armazena a chave do Firebase como ID
                        listaPacientes.add(paciente);
                    }
                }

                binding.progressBarVerPacientes.setVisibility(View.GONE);

                if (listaPacientes.isEmpty()) {
                    showEmptyState("Nenhum paciente encontrado.");
                } else {
                    showRecyclerView();
                    if (adapter == null) {
                        // --- MUDANÇA 2: Passar 'this' (o fragment) como listener para o Adapter ---
                        adapter = new PacienteAdapter(listaPacientes, VerPacientesFragment.this);
                        binding.recyclerViewPacientes.setAdapter(adapter);
                    } else {
                        // Notifica o adapter que a lista mudou (mais eficiente que filtrar de novo)
                        adapter.updateList(listaPacientes);
                        adapter.getFilter().filter(binding.etPesquisarPaciente.getText().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded()) {
                    showEmptyState("Erro ao carregar dados: " + databaseError.getMessage());
                }
            }
        };
        userPacientesRef.addValueEventListener(pacientesValueEventListener);
    }

    private void setupSearch() {
        binding.etPesquisarPaciente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showRecyclerView() {
        binding.recyclerViewPacientes.setVisibility(View.VISIBLE);
        binding.tvListaVazia.setVisibility(View.GONE);
    }

    private void showEmptyState(String message) {
        binding.progressBarVerPacientes.setVisibility(View.GONE);
        binding.recyclerViewPacientes.setVisibility(View.GONE);
        binding.tvListaVazia.setVisibility(View.VISIBLE);
        binding.tvListaVazia.setText(message);
    }

    // --- MUDANÇA 3: Implementação dos métodos da interface ---
    @Override
    public void onEditClick(Paciente paciente) {
        // Cria a ação de navegação segura
        VerPacientesFragmentDirections.ActionVerPacientesFragmentToAdicionarPacientesFragment action =
                VerPacientesFragmentDirections.actionVerPacientesFragmentToAdicionarPacientesFragment();

        // Passa o ID do paciente (argumento opcional)
        action.setPacienteId(paciente.getId());

        // Executa a navegação
        navController.navigate(action);
    }


    @Override
    public void onDeleteClick(Paciente paciente) {
        // Verifica se o contexto é válido antes de criar o diálogo
        if (getContext() == null) return;

        // Mostra o diálogo de confirmação para exclusão
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir Paciente")
                .setMessage("Tem certeza que deseja excluir " + paciente.getNome() + "? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    // Lógica para excluir o paciente do Firebase
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseDatabase.getInstance().getReference("pacientes")
                                .child(user.getUid())
                                .child(paciente.getId())
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    if(isAdded()) Toast.makeText(getContext(), "Paciente excluído.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    if(isAdded()) Toast.makeText(getContext(), "Erro ao excluir.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove o listener do Firebase para evitar memory leaks e crashes
        if (userPacientesRef != null && pacientesValueEventListener != null) {
            userPacientesRef.removeEventListener(pacientesValueEventListener);
        }
        binding = null; // Limpa a referência ao binding
    }
}

