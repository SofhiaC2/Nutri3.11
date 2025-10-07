package com.example.nutri3.fragments.consultas; // Mantive seu pacote, mas "consultas" parece estranho para "AdicionarPacientes"

import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentAdicionarPacientesBinding;
import com.example.nutri3.fragments.consultas.Paciente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdicionarPacientesFragment extends Fragment {

    // Declaração do objeto de View Binding
    private FragmentAdicionarPacientesBinding binding;

    private Calendar calendario;
    private NavController navController;

    // Construtor público vazio é necessário
    public AdicionarPacientesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdicionarPacientesBinding.inflate(inflater, container, false);
        try {
            navController = NavHostFragment.findNavController(this);
        } catch (IllegalStateException e) {
            Toast.makeText(getContext(), "Erro ao encontrar NavController", Toast.LENGTH_LONG).show();
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        calendario = Calendar.getInstance();

        if (navController == null) {
            try {
                navController = NavHostFragment.findNavController(this);
            } catch (IllegalStateException e) {
                Toast.makeText(getContext(), "NavController não encontrado em onViewCreated", Toast.LENGTH_LONG).show();
            }
        }

        setupToolbar();
        setupDatePicker();
        setupGeneroSpinner();
        setupSaveButton();
    }

    private void setupToolbar() {
        binding.btnAddPacienteVoltar.setOnClickListener(v -> {
            if (navController != null) {
                navController.popBackStack();
            } else if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

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

    private void setupSaveButton() {
        binding.btnSalvarPaciente.setOnClickListener(v -> {
            if (validarCampos()) {
                salvarPaciente();
            }
        });
    }

    private boolean validarCampos() {
        if (binding == null || getContext() == null) return false;

        String nome = binding.etNomePaciente.getText().toString().trim();
        String dataNasc = binding.etDataNascPaciente.getText().toString().trim();
        String email = binding.etEmailPaciente.getText().toString().trim();
        String telefone = binding.etTelefonePaciente.getText().toString().trim();

        if (TextUtils.isEmpty(nome)) {
            binding.etNomePaciente.setError("Nome é obrigatório");
            binding.etNomePaciente.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(dataNasc)) {
            binding.etDataNascPaciente.setError("Data de nascimento é obrigatória");
            Toast.makeText(getContext(), "Data de nascimento é obrigatória", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            binding.etEmailPaciente.setError("Email é obrigatório");
            binding.etEmailPaciente.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailPaciente.setError("Insira um email válido");
            binding.etEmailPaciente.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(telefone)) {
            binding.etTelefonePaciente.setError("Telefone é obrigatório");
            binding.etTelefonePaciente.requestFocus();
            return false;
        }
        if (binding.spinnerGeneroPaciente.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Selecione um gênero válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void salvarPaciente() {
        if (binding == null || getContext() == null) {
            Toast.makeText(getContext(), "Erro interno. Tente novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Obter o usuário logado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Nenhum usuário logado. Faça o login primeiro.", Toast.LENGTH_LONG).show();
            // TODO: Opcional - redirecionar para a tela de login
            // navController.navigate(R.id.action_adicionarPacientesFragment_to_loginFragment);
            return;
        }
        String userId = currentUser.getUid();

        // 2. Coletar os dados do formulário
        String nome = binding.etNomePaciente.getText().toString().trim();
        String dataNasc = binding.etDataNascPaciente.getText().toString().trim();
        String email = binding.etEmailPaciente.getText().toString().trim();
        String telefone = binding.etTelefonePaciente.getText().toString().trim();
        String genero = binding.spinnerGeneroPaciente.getSelectedItem().toString();
        String observacoes = binding.etObservacoesPaciente.getText().toString().trim();

        // 3. Obter a referência correta do Database, usando o ID do usuário
        DatabaseReference userPacientesRef = FirebaseDatabase.getInstance()
                .getReference("pacientes") // Nó raiz para todos os pacientes
                .child(userId);           // Sub-nó específico para o usuário logado

        // 4. Gerar uma chave única para o novo paciente
        String pacienteId = userPacientesRef.push().getKey();
        if (pacienteId == null) {
            Toast.makeText(getContext(), "Não foi possível gerar ID para o paciente.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Criar o objeto Paciente
        // Certifique-se de que sua classe Paciente tem um construtor que aceita esses parâmetros,
        // incluindo o 'userId' para o campo 'nutricionistaId'.
        Paciente novoPaciente = new Paciente(nome, dataNasc, email, telefone, genero, observacoes, userId);

        // 6. Salvar o objeto Paciente no Firebase
        binding.btnSalvarPaciente.setEnabled(false);
        binding.btnSalvarPaciente.setText("Salvando...");

        userPacientesRef.child(pacienteId).setValue(novoPaciente)
                .addOnSuccessListener(aVoid -> {
                    // Sucesso!
                    Toast.makeText(getContext(), "Paciente salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    if (navController != null) {
                        navController.popBackStack(); // Volta para a tela anterior
                    }
                })
                .addOnFailureListener(e -> {
                    // Falha!
                    Toast.makeText(getContext(), "Erro ao salvar paciente: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Reativar o botão em caso de falha
                    if(binding != null) { // Verifica se o binding ainda é válido
                        binding.btnSalvarPaciente.setEnabled(true);
                        binding.btnSalvarPaciente.setText("Salvar Paciente");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpa a referência ao binding
    }
}
