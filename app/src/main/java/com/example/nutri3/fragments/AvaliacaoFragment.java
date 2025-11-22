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
import java.util.Locale;

public class AvaliacaoFragment extends Fragment {

    private static final String TAG = "AvaliacaoFragment";
    private FragmentAvaliacaoBinding binding;
    private NavController navController;
    private ConsultaViewModel consultaViewModel;

    private String pacienteId;
    private String nomePaciente;
    private String generoPaciente;
    private String dataNascimentoPaciente;

    public AvaliacaoFragment() {}

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

        // Busca o ID do paciente do ViewModel
        consultaViewModel.getPacienteIdSelecionado().observe(getViewLifecycleOwner(), id -> {
            if (id == null) {
                Toast.makeText(getContext(), "Erro: ID do paciente não encontrado. Voltando...", Toast.LENGTH_LONG).show();
                navController.popBackStack();
            } else {
                this.pacienteId = id;
                buscarDadosDoPaciente();
            }
        });

        // Observa os dados da avaliação no ViewModel para preencher a tela ao voltar
        consultaViewModel.getDadosAvaliacao().observe(getViewLifecycleOwner(), avaliacao -> {
            if (avaliacao != null) {
                preencherCamposComDadosDoViewModel(avaliacao);
            }
        });

        // A visibilidade do botão é controlada pelo layout da toolbar agora, então a linha abaixo foi removida.
        // binding.btnAvancar.setVisibility(View.VISIBLE);

        setupCalculosTempoReal();
        setupClickListeners();
    }

    private void buscarDadosDoPaciente() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || pacienteId == null) return;

        DatabaseReference pacienteRef = FirebaseDatabase.getInstance().getReference("pacientes")
                .child(currentUser.getUid()).child(pacienteId);

        pacienteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    nomePaciente = snapshot.child("nome").getValue(String.class);
                    generoPaciente = snapshot.child("genero").getValue(String.class);
                    dataNascimentoPaciente = snapshot.child("dataNascimento").getValue(String.class);

                    if (nomePaciente != null) {
                        // **CORREÇÃO**: Atualiza o TextView do título dentro da Toolbar
                        binding.tvTitulo.setText("Avaliação de " + nomePaciente.split(" ")[0]);
                        atualizarVisibilidadeCamposPorGenero();
                        calcularIMCEPercentualGordura();
                    } else {
                        Toast.makeText(getContext(), "Nome do paciente não encontrado.", Toast.LENGTH_LONG).show();
                        navController.popBackStack();
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

    private void setupClickListeners() {
        // **CORREÇÃO**: O listener de voltar agora é no ícone de navegação da Toolbar
        binding.toolbarAvaliacao.setNavigationOnClickListener(v -> navController.popBackStack());

        // O listener do botão "Avançar" continua o mesmo, pois o ID do botão é o mesmo
        binding.btnAvancar.setOnClickListener(v -> {
            salvarDadosNoViewModel();
            navController.navigate(R.id.action_avaliacaoFragment_to_dietaFragment);
        });
    }

    private void salvarDadosNoViewModel() {
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
            dadosAtuais.setCristaIliaca(parseDoubleOrZero(binding.etCristaIliaca.getText().toString()));
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter dados para salvar no ViewModel", e);
        }
        consultaViewModel.atualizarDadosAvaliacao(dadosAtuais);
    }

    private void preencherCamposComDadosDoViewModel(Avaliacao avaliacao) {
        if (avaliacao.getPeso() > 0) binding.etPeso.setText(String.format(Locale.US, "%.2f", avaliacao.getPeso()));
        if (avaliacao.getAltura() > 0) binding.etAltura.setText(String.format(Locale.US, "%.2f", avaliacao.getAltura()));
        if (avaliacao.getTriceps() > 0) binding.etTriceps.setText(String.format(Locale.US, "%.1f", avaliacao.getTriceps()));
        if (avaliacao.getSuprailiaca() > 0) binding.etSuprailiaca.setText(String.format(Locale.US, "%.1f", avaliacao.getSuprailiaca()));
        if (avaliacao.getCoxa() > 0) binding.etCoxa.setText(String.format(Locale.US, "%.1f", avaliacao.getCoxa()));
        if (avaliacao.getSubescapular() > 0) binding.etSubescapular.setText(String.format(Locale.US, "%.1f", avaliacao.getSubescapular()));
        if (avaliacao.getAbdominal() > 0) binding.etAbdominal.setText(String.format(Locale.US, "%.1f", avaliacao.getAbdominal()));
        if (avaliacao.getPeitoral() > 0) binding.etPeitoral.setText(String.format(Locale.US, "%.1f", avaliacao.getPeitoral()));
        if (avaliacao.getAxilar() > 0) binding.etAxilar.setText(String.format(Locale.US, "%.1f", avaliacao.getAxilar()));
        if (avaliacao.getCristaIliaca() > 0) binding.etCristaIliaca.setText(String.format(Locale.US, "%.1f", avaliacao.getCristaIliaca()));
    }

    @Override
    public void onPause() {
        super.onPause();
        salvarDadosNoViewModel();
    }

    //<editor-fold desc="Cálculos e helpers">
    private void setupCalculosTempoReal() {
        TextWatcher calculosWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                calcularIMCEPercentualGordura();
            }
        };
        binding.etPeso.addTextChangedListener(calculosWatcher);
        binding.etAltura.addTextChangedListener(calculosWatcher);
        binding.etTriceps.addTextChangedListener(calculosWatcher);
        binding.etSuprailiaca.addTextChangedListener(calculosWatcher);
        binding.etCoxa.addTextChangedListener(calculosWatcher);
        binding.etSubescapular.addTextChangedListener(calculosWatcher);
        binding.etAbdominal.addTextChangedListener(calculosWatcher);
        binding.etPeitoral.addTextChangedListener(calculosWatcher);
        binding.etCristaIliaca.addTextChangedListener(calculosWatcher);
        binding.etAxilar.addTextChangedListener(calculosWatcher);
    }

    private void calcularIMCEPercentualGordura() {
        double peso = parseDoubleOrZero(binding.etPeso.getText().toString());
        double altura = parseDoubleOrZero(binding.etAltura.getText().toString());
        if (altura > 0 && peso > 0) {
            binding.tvIMC.setText(String.format(Locale.US, "%.1f", peso / (altura * altura)));
        } else {
            binding.tvIMC.setText("0.0");
        }

        if (generoPaciente == null || TextUtils.isEmpty(dataNascimentoPaciente)) {
            binding.tvPercentualGordura.setText("0.0%");
            return;
        }

        double somaDobras;
        if (generoPaciente.equalsIgnoreCase("Masculino")) {
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

        int idade = calcularIdade(dataNascimentoPaciente);
        if (idade <= 0) {
            binding.tvPercentualGordura.setText("0.0%");
            return;
        }

        double densidadeCorporal;
        if (generoPaciente.equalsIgnoreCase("Masculino")) {
            densidadeCorporal = 1.112 - (0.00043499 * somaDobras) + (0.00000055 * (somaDobras * somaDobras)) - (0.00028826 * idade);
        } else {
            densidadeCorporal = 1.097 - (0.00046971 * somaDobras) + (0.00000056 * (somaDobras * somaDobras)) - (0.00012828 * idade);
        }

        double percentualGordura = ((4.95 / densidadeCorporal) - 4.50) * 100;

        if (percentualGordura > 0 && percentualGordura < 100) {
            binding.tvPercentualGordura.setText(String.format(Locale.US, "%.1f%%", percentualGordura));
        } else {
            binding.tvPercentualGordura.setText("0.0%");
        }
    }

    private int calcularIdade(String dataNascimento) {
        if (TextUtils.isEmpty(dataNascimento)) return 0;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate birthDate = LocalDate.parse(dataNascimento, formatter);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (DateTimeParseException e) {
            return 0;
        }
    }

    private double parseDoubleOrZero(String value) {
        if (TextUtils.isEmpty(value)) return 0.0;
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void atualizarVisibilidadeCamposPorGenero() {
        if (generoPaciente == null) return;
        if (generoPaciente.equalsIgnoreCase("Masculino")) {
            binding.tilPeitoral.setVisibility(View.VISIBLE);
            binding.tilCristaIliaca.setVisibility(View.GONE);
        } else {
            binding.tilPeitoral.setVisibility(View.GONE);
            binding.tilCristaIliaca.setVisibility(View.VISIBLE);
        }
    }
    //</editor-fold>

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
