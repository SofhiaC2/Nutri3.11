package com.example.nutri3.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import com.example.nutri3.R;
import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.databinding.FragmentAvaliacaoBinding;
import com.example.nutri3.model.Avaliacao;
import com.example.nutri3.fragments.consultas.Paciente;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AvaliacaoFragment extends Fragment {

    private static final String TAG = "AvaliacaoFragment";
    private FragmentAvaliacaoBinding binding;
    private NavController navController;
    private ConsultaViewModel consultaViewModel;
    private String pacienteId;
    private Paciente pacienteAtual;
    private boolean navegarParaDietaAposConcluir;

    public AvaliacaoFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pacienteId = AvaliacaoFragmentArgs.fromBundle(getArguments()).getPacienteId();
            navegarParaDietaAposConcluir = AvaliacaoFragmentArgs.fromBundle(getArguments()).getNavegarParaDietaAposConcluir();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAvaliacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);
        binding.btnVoltar.setOnClickListener(v -> navController.popBackStack());
        buscarDadosDoPaciente();
        setupCalculosTempoReal();
        setupClickListeners();
    }

    private void buscarDadosDoPaciente() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || pacienteId == null) {
            Toast.makeText(getContext(), "Erro: Paciente ou usuário não identificado.", Toast.LENGTH_LONG).show();
            navController.popBackStack();
            return;
        }
        DatabaseReference pacienteRef = FirebaseDatabase.getInstance().getReference("pacientes")
                .child(currentUser.getUid()).child(pacienteId);

        pacienteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    pacienteAtual = snapshot.getValue(Paciente.class);
                    if (pacienteAtual != null) {
                        Log.d(TAG, "Paciente carregado. Gênero: " + pacienteAtual.getGenero());
                        binding.tvTitulo.setText("Avaliação de " + pacienteAtual.getNome().split(" ")[0]);
                        // MUDANÇA: Controla a visibilidade dos campos aqui
                        atualizarVisibilidadeCamposPorGenero();
                    }
                } else {
                    Toast.makeText(getContext(), "Não foi possível encontrar os dados do paciente.", Toast.LENGTH_LONG).show();
                    navController.popBackStack();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erro ao carregar dados do paciente.", error.toException());
                navController.popBackStack();
            }
        });
    }

    private void atualizarVisibilidadeCamposPorGenero() {
        if (pacienteAtual == null || pacienteAtual.getGenero() == null) return;

        if (pacienteAtual.getGenero().equalsIgnoreCase("Masculino")) {
            binding.tilPeitoral.setVisibility(View.VISIBLE);
            binding.tilCristaIliaca.setVisibility(View.GONE);
        } else { // Feminino
            binding.tilPeitoral.setVisibility(View.GONE);
            binding.tilCristaIliaca.setVisibility(View.VISIBLE);
        }
    }

    private void setupCalculosTempoReal() {
        TextWatcher calculosWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                calcularIMCEPercentualGordura();
            }
        };

        // Aplica o listener a todos os campos
        binding.etPeso.addTextChangedListener(calculosWatcher);
        binding.etAltura.addTextChangedListener(calculosWatcher);
        binding.etTriceps.addTextChangedListener(calculosWatcher);
        binding.etSuprailiaca.addTextChangedListener(calculosWatcher);
        binding.etCoxa.addTextChangedListener(calculosWatcher);
        binding.etSubescapular.addTextChangedListener(calculosWatcher);
        binding.etAbdominal.addTextChangedListener(calculosWatcher);
        binding.etPeitoral.addTextChangedListener(calculosWatcher);
        binding.etCristaIliaca.addTextChangedListener(calculosWatcher); // Adicionado
        binding.etAxilar.addTextChangedListener(calculosWatcher);
    }

    private void calcularIMCEPercentualGordura() {
        double peso = parseDoubleOrZero(binding.etPeso.getText().toString());
        double altura = parseDoubleOrZero(binding.etAltura.getText().toString());
        if (altura > 0 && peso > 0) {
            binding.tvIMC.setText(String.format("%.1f", peso / (altura * altura)));
        } else {
            binding.tvIMC.setText("0.0");
        }

        if (pacienteAtual == null || pacienteAtual.getGenero() == null || TextUtils.isEmpty(pacienteAtual.getDataNascimento())) {
            binding.tvPercentualGordura.setText("0.0%");
            return;
        }

        // MUDANÇA: Soma das dobras agora é específica para cada gênero
        double somaDobras;
        if (pacienteAtual.getGenero().equalsIgnoreCase("Masculino")) {
            somaDobras = parseDoubleOrZero(binding.etPeitoral.getText().toString()) +
                    parseDoubleOrZero(binding.etAbdominal.getText().toString()) +
                    parseDoubleOrZero(binding.etCoxa.getText().toString()) +
                    parseDoubleOrZero(binding.etTriceps.getText().toString()) +
                    parseDoubleOrZero(binding.etSubescapular.getText().toString()) +
                    parseDoubleOrZero(binding.etSuprailiaca.getText().toString()) +
                    parseDoubleOrZero(binding.etAxilar.getText().toString());
        } else { // Feminino
            somaDobras = parseDoubleOrZero(binding.etCristaIliaca.getText().toString()) +
                    parseDoubleOrZero(binding.etAbdominal.getText().toString()) +
                    parseDoubleOrZero(binding.etCoxa.getText().toString()) +
                    parseDoubleOrZero(binding.etTriceps.getText().toString()) +
                    parseDoubleOrZero(binding.etSubescapular.getText().toString()) +
                    parseDoubleOrZero(binding.etSuprailiaca.getText().toString()) +
                    parseDoubleOrZero(binding.etAxilar.getText().toString());
        }


        if (somaDobras <= 0) {
            binding.tvPercentualGordura.setText("0.0%");
            return;
        }

        int idade = calcularIdade(pacienteAtual.getDataNascimento());
        if (idade <= 0) {
            binding.tvPercentualGordura.setText("0.0%");
            return;
        }

        double densidadeCorporal;
        if (pacienteAtual.getGenero().equalsIgnoreCase("Masculino")) {
            densidadeCorporal = 1.112 - (0.00043499 * somaDobras) + (0.00000055 * (somaDobras * somaDobras)) - (0.00028826 * idade);
        } else { // Feminino
            densidadeCorporal = 1.097 - (0.00046971 * somaDobras) + (0.00000056 * (somaDobras * somaDobras)) - (0.00012828 * idade);
        }

        double percentualGordura = ((4.95 / densidadeCorporal) - 4.50) * 100;

        if (percentualGordura > 0 && percentualGordura < 100) {
            binding.tvPercentualGordura.setText(String.format("%.1f%%", percentualGordura));
        } else {
            binding.tvPercentualGordura.setText("0.0%");
        }
    }

    private int calcularIdade(String dataNascimento) {
        if (TextUtils.isEmpty(dataNascimento)) return 0;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate birthDate = LocalDate.parse(dataNascimento, formatter);
            LocalDate currentDate = LocalDate.now();
            return Period.between(birthDate, currentDate).getYears();
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    private void setupClickListeners() {
        binding.btnSalvarAvaliacao.setOnClickListener(v -> {
            if (pacienteAtual == null) {
                Toast.makeText(getContext(), "Aguarde os dados do paciente serem carregados.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validarEsalvarDados(true)) return;
            Avaliacao avaliacaoCompleta = consultaViewModel.getAvaliacao().getValue();
            if (avaliacaoCompleta != null) {
                salvarAvaliacaoNoFirebase(avaliacaoCompleta);
            }
        });
    }

    private void salvarAvaliacaoNoFirebase(Avaliacao avaliacao) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("avaliacoes")
                .child(currentUser.getUid()).child(pacienteId).push();

        ref.setValue(avaliacao).addOnSuccessListener(aVoid -> {
            if (navegarParaDietaAposConcluir) {
                Toast.makeText(getContext(), "Avaliação salva. Próximo passo: Plano Alimentar.", Toast.LENGTH_SHORT).show();
                AvaliacaoFragmentDirections.ActionAvaliacaoFragmentToDietaFragment action =
                        AvaliacaoFragmentDirections.actionAvaliacaoFragmentToDietaFragment(pacienteId);
                navController.navigate(action);
            } else {
                Toast.makeText(getContext(), "Avaliação salva com sucesso!", Toast.LENGTH_SHORT).show();
                if (consultaViewModel != null) consultaViewModel.limparDados();
                navController.popBackStack(R.id.menu_home, false);
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Falha ao salvar a avaliação.", Toast.LENGTH_SHORT).show());
    }

    private boolean validarEsalvarDados(boolean validarObrigatorios) {
        if (validarObrigatorios) {
            if (TextUtils.isEmpty(binding.etPeso.getText()) || TextUtils.isEmpty(binding.etAltura.getText())) {
                Toast.makeText(getContext(), "Peso e Altura são obrigatórios.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Avaliacao dadosAtuais = new Avaliacao();
        try {
            dadosAtuais.setPeso(parseDoubleOrZero(binding.etPeso.getText().toString()));
            dadosAtuais.setAltura(parseDoubleOrZero(binding.etAltura.getText().toString()));
            dadosAtuais.setTriceps(parseDoubleOrZero(binding.etTriceps.getText().toString()));
            dadosAtuais.setSuprailiaca(parseDoubleOrZero(binding.etSuprailiaca.getText().toString()));
            dadosAtuais.setCoxa(parseDoubleOrZero(binding.etCoxa.getText().toString()));
            dadosAtuais.setSubescapular(parseDoubleOrZero(binding.etSubescapular.getText().toString()));
            dadosAtuais.setAbdominal(parseDoubleOrZero(binding.etAbdominal.getText().toString()));
            dadosAtuais.setPeitoral(parseDoubleOrZero(binding.etPeitoral.getText().toString()));
            dadosAtuais.setAxilar(parseDoubleOrZero(binding.etAxilar.getText().toString()));
            dadosAtuais.setCristaIliaca(parseDoubleOrZero(binding.etCristaIliaca.getText().toString())); // Adicionado
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erro ao processar os dados.", Toast.LENGTH_SHORT).show();
            return false;
        }
        consultaViewModel.setAvaliacao(dadosAtuais);
        return true;
    }

    private double parseDoubleOrZero(String value) {
        if (TextUtils.isEmpty(value)) return 0.0;
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
