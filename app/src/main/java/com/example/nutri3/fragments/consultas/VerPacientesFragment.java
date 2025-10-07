package com.example.nutri3.fragments.consultas; // Ou o pacote correto do seu fragmento

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutri3.R;
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

public class VerPacientesFragment extends Fragment {

    private FragmentVerPacientesBinding binding;
    private NavController navController;
    private PacienteAdapter adapter;
    private final List<Paciente> listaPacientes = new ArrayList<>();

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
        DatabaseReference userPacientesRef = FirebaseDatabase.getInstance().getReference("pacientes").child(userId);

        userPacientesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                        adapter = new PacienteAdapter(listaPacientes);
                        binding.recyclerViewPacientes.setAdapter(adapter);
                    } else {
                        // Se o adapter já existe, atualize os dados (útil para atualizações em tempo real)
                        adapter.getFilter().filter(binding.etPesquisarPaciente.getText().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showEmptyState("Erro ao carregar dados: " + databaseError.getMessage());
            }
        });
    }

    private void setupSearch() {
        binding.etPesquisarPaciente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // A cada letra digitada, o filtro do adapter é chamado
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpa a referência ao binding
    }
}

