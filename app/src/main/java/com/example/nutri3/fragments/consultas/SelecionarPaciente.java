package com.example.nutri3.fragments.consultas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutri3.R;
import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.adapters.PacienteAdapter;
import com.example.nutri3.databinding.FragmentVerPacientesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelecionarPaciente extends Fragment implements PacienteAdapter.OnPacienteInteractionListener {

    private FragmentVerPacientesBinding binding;
    private NavController navController;
    private PacienteAdapter adapter;
    private final List<Paciente> listaPacientes = new ArrayList<>();
    private ValueEventListener pacientesValueEventListener;
    private DatabaseReference userPacientesRef;

    private ConsultaViewModel consultaViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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

        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        carregarPacientesDoFirebase();
    }


    @Override
    public void onSelectClick(Paciente paciente) {
        if (paciente == null || paciente.getId() == null) {
            Toast.makeText(getContext(), "Erro: Paciente inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("SelecionarPaciente", "Paciente selecionado: " + paciente.getNome() + " com ID: " + paciente.getId());

        consultaViewModel.selecionarPaciente(paciente.getId());

        navController.navigate(R.id.action_selecionarPaciente_to_avaliacaoFragment);
    }

    @Override
    public void onEditClick(Paciente paciente) {}

    @Override
    public void onDeleteClick(Paciente paciente) {}

    private void setupToolbar() {
        binding.btnVerPacientesVoltar.setOnClickListener(v -> navController.popBackStack());
        binding.tvToolbarTitleVerPacientes.setText("Selecione o Paciente");
    }

    private void setupRecyclerView() {
        binding.recyclerViewPacientes.setLayoutManager(new LinearLayoutManager(getContext()));
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

        if (pacientesValueEventListener != null) {
            userPacientesRef.removeEventListener(pacientesValueEventListener);
        }

        pacientesValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                listaPacientes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Paciente paciente = snapshot.getValue(Paciente.class);
                    if (paciente != null) {
                        paciente.setId(snapshot.getKey());
                        listaPacientes.add(paciente);
                    }
                }

                binding.progressBarVerPacientes.setVisibility(View.GONE);

                if (listaPacientes.isEmpty()) {
                    showEmptyState("Nenhum paciente encontrado.");
                } else {
                    showRecyclerView();
                    if (adapter == null) {
                        adapter = new PacienteAdapter(listaPacientes, R.layout.item_pacienteselect, SelecionarPaciente.this);
                        binding.recyclerViewPacientes.setAdapter(adapter);
                    } else {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userPacientesRef != null && pacientesValueEventListener != null) {
            userPacientesRef.removeEventListener(pacientesValueEventListener);
        }
        binding = null;
    }
}
