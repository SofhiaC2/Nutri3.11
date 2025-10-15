package com.example.nutri3.fragments.consultas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentAdicionarPacientesBinding;
import com.example.nutri3.fragments.consultas.Paciente; // Verifique se o import do modelo Paciente está correto
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdicionarPacientesFragment extends Fragment {

    private FragmentAdicionarPacientesBinding binding;
    private Calendar calendario;
    private NavController navController;
    private DatabaseReference userPacientesRef;

    // Variáveis para controlar o modo
    private boolean isEditMode = false;
    private String pacienteIdParaEditar = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Verifica se estamos em modo de edição
        if (getArguments() != null) {
            pacienteIdParaEditar = AdicionarPacientesFragmentArgs.fromBundle(getArguments()).getPacienteId();
            if (pacienteIdParaEditar != null) {
                isEditMode = true;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdicionarPacientesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        calendario = Calendar.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Usuário não encontrado.", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
            return;
        }
        userPacientesRef = FirebaseDatabase.getInstance().getReference("pacientes").child(currentUser.getUid());

        setupDatePicker();
        setupGeneroSpinner();
        setupToolbar();

        // 2. Configura a UI com base no modo (Adicionar ou Editar)
        if (isEditMode) {
            setupEditMode();
        } else {
            setupAddMode();
        }
    }

    private void setupToolbar() {
        binding.btnAddPacienteVoltar.setOnClickListener(v -> navController.popBackStack());
    }

    private void setupAddMode() {
        binding.tvToolbarTitleAddPaciente.setText("Adicionar Paciente");
        binding.btnSalvarPaciente.setText("Salvar Paciente");
        binding.btnSalvarPaciente.setOnClickListener(v -> {
            if (validarCampos()) {
                salvarNovoPaciente();
            }
        });
    }

    private void setupEditMode() {
        binding.tvToolbarTitleAddPaciente.setText("Editar Paciente");
        binding.btnSalvarPaciente.setText("Salvar Alterações");

        userPacientesRef.child(pacienteIdParaEditar).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    Paciente paciente = snapshot.getValue(Paciente.class);
                    if (paciente != null) {
                        binding.etNomePaciente.setText(paciente.getNome());
                        binding.etDataNascPaciente.setText(paciente.getDataNascimento());
                        binding.etEmailPaciente.setText(paciente.getEmail());
                        binding.etTelefonePaciente.setText(paciente.getTelefone());
                        binding.etObservacoesPaciente.setText(paciente.getObservacoes());

                        if (paciente.getGenero() != null) {
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.spinnerGeneroPaciente.getAdapter();
                            int position = adapter.getPosition(paciente.getGenero());
                            binding.spinnerGeneroPaciente.setSelection(position);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(isAdded()) Toast.makeText(getContext(), "Falha ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSalvarPaciente.setOnClickListener(v -> {
            if (validarCampos()) {
                atualizarPaciente();
            }
        });
    }

    private void salvarNovoPaciente() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String pacienteId = userPacientesRef.push().getKey();
        if (pacienteId == null) {
            Toast.makeText(getContext(), "Erro ao criar ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        Paciente novoPaciente = criarPacienteDoFormulario(userId);
        setSavingState(true, "Salvando...");

        userPacientesRef.child(pacienteId).setValue(novoPaciente)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Paciente salvo!", Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Falha ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setSavingState(false, "Salvar Paciente");
                });
    }

    private void atualizarPaciente() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nome", binding.etNomePaciente.getText().toString().trim());
        updates.put("dataNasc", binding.etDataNascPaciente.getText().toString().trim());
        updates.put("email", binding.etEmailPaciente.getText().toString().trim());
        updates.put("telefone", binding.etTelefonePaciente.getText().toString().trim());
        updates.put("genero", binding.spinnerGeneroPaciente.getSelectedItem().toString());
        updates.put("observacoes", binding.etObservacoesPaciente.getText().toString().trim());

        setSavingState(true, "Salvando...");

        userPacientesRef.child(pacienteIdParaEditar).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Paciente atualizado!", Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Falha ao atualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setSavingState(false, "Salvar Alterações");
                });
    }

    private void setSavingState(boolean isSaving, String buttonText) {
        if (binding != null) {
            binding.btnSalvarPaciente.setEnabled(!isSaving);
            binding.btnSalvarPaciente.setText(buttonText);
        }
    }

    private Paciente criarPacienteDoFormulario(String nutricionistaId) {
        String nome = binding.etNomePaciente.getText().toString().trim();
        String dataNasc = binding.etDataNascPaciente.getText().toString().trim();
        String email = binding.etEmailPaciente.getText().toString().trim();
        String telefone = binding.etTelefonePaciente.getText().toString().trim();
        String genero = binding.spinnerGeneroPaciente.getSelectedItem().toString();
        String observacoes = binding.etObservacoesPaciente.getText().toString().trim();
        return new Paciente(nome, dataNasc, email, telefone, genero, observacoes, nutricionistaId);
    }

    // Seus métodos de setup e validação permanecem os mesmos
    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            calendario.set(Calendar.YEAR, year);
            calendario.set(Calendar.MONTH, month);
            calendario.set(Calendar.DAY_OF_MONTH, day);
            atualizarCampoData();
        };

        binding.etDataNascPaciente.setOnClickListener(v -> {
            if (getContext() != null) {
                new DatePickerDialog(requireContext(), dateSetListener,
                        calendario.get(Calendar.YEAR),
                        calendario.get(Calendar.MONTH),
                        calendario.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
    private void atualizarCampoData() {
        String formatoData = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(formatoData, Locale.getDefault());
        binding.etDataNascPaciente.setText(sdf.format(calendario.getTime()));
    }
    private void setupGeneroSpinner() {
        if (getContext() == null) return;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.generos_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGeneroPaciente.setAdapter(adapter);
    }
    private boolean validarCampos() {
        // Seu método de validação...
        return true; // Simplificado para o exemplo
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
