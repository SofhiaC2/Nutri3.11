package com.example.nutri3;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.databinding.DialogQuantidadeAlimentogBinding;
import com.example.nutri3.model.Alimento;
import java.io.Serializable;
import java.util.Locale;

public class DialogQuantidadeG extends DialogFragment {

    private static final String ARG_ALIMENTO = "alimento";
    private static final String ARG_TIPO_REFEICAO = "tipo_refeicao";

    private DialogQuantidadeAlimentogBinding binding;
    private ConsultaViewModel consultaViewModel;
    private Alimento alimento;
    private String tipoRefeicao;

    public static DialogQuantidadeG newInstance(Alimento alimento, String tipoRefeicao) {
        DialogQuantidadeG fragment = new DialogQuantidadeG();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALIMENTO, (Serializable) alimento);
        args.putString(ARG_TIPO_REFEICAO, tipoRefeicao);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            alimento = (Alimento) getArguments().getSerializable(ARG_ALIMENTO);
            tipoRefeicao = getArguments().getString(ARG_TIPO_REFEICAO);
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Nutri3_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogQuantidadeAlimentogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        if (alimento == null) {
            dismiss();
            return;
        }

        setupInitialViews();
        setupListeners();
    }

    private void setupInitialViews() {
        binding.tvNomeAlimento.setText(alimento.getNome());
        binding.tvPorcaoBase.setText(String.format(Locale.getDefault(), "Valores por %s", alimento.getPorcaoBase()));

        atualizarValoresNutricionais(0);
    }

    private void setupListeners() {
        binding.etQuantidadeGrama.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double quantidade = Double.parseDouble(s.toString());
                    atualizarValoresNutricionais(quantidade);
                } catch (NumberFormatException e) {
                    atualizarValoresNutricionais(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnAdicionar.setOnClickListener(v -> {
            try {
                double quantidadeFinal = Double.parseDouble(binding.etQuantidadeGrama.getText().toString());
                if (quantidadeFinal > 0) {
                    alimento.calcularNutrientesPorPorcao(quantidadeFinal);
                    consultaViewModel.adicionarAlimento(alimento, tipoRefeicao);
                    dismiss();
                }
            } catch (NumberFormatException e) {
                // Não faz nada se o campo estiver vazio
            }
        });
    }

    private void atualizarValoresNutricionais(double quantidade) {
        alimento.calcularNutrientesPorPorcao(quantidade);

        binding.tvKcalCalculado.setText(String.format(Locale.getDefault(), "Calorias: %.0f kcal", alimento.getEnergiaCalculada()));
        // Você pode adicionar outros TextViews para Carboidratos, Proteínas, etc. e atualizá-los aqui.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Evita memory leaks
    }
}
