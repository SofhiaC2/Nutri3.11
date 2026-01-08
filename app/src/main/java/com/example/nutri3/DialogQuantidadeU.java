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
// Corrigido para o seu nome de binding: DialogQuantidadeAlimentouBinding
import com.example.nutri3.databinding.DialogQuantidadeAlimentouBinding;
import com.example.nutri3.model.Alimento;
import java.io.Serializable;
import java.util.Locale;

public class DialogQuantidadeU extends DialogFragment {

    private static final String ARG_ALIMENTO = "alimento";
    private static final String ARG_TIPO_REFEICAO = "tipo_refeicao";

    // O tipo do binding deve corresponder ao nome do seu arquivo XML.
    private DialogQuantidadeAlimentouBinding binding;
    private ConsultaViewModel consultaViewModel;
    private Alimento alimento;
    private String tipoRefeicao;

    // AQUI ESTÁ A CORREÇÃO PRINCIPAL
    public static DialogQuantidadeU newInstance(Alimento alimento, String tipoRefeicao) {
        // Retorna um objeto do tipo "DialogQuantidadeU", o nome da sua classe.
        DialogQuantidadeU fragment = new DialogQuantidadeU();
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
        // Aplica um estilo para que o dialog não ocupe a tela inteira
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Nutri3_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Usa View Binding para acessar os componentes do layout de forma segura
        binding = DialogQuantidadeAlimentouBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Obtém a instância do ViewModel compartilhado pela Activity
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        // Se, por algum motivo, o alimento for nulo, fecha o dialog
        if (alimento == null) {
            dismiss();
            return;
        }

        setupInitialViews();
        setupListeners();
    }

    private void setupInitialViews() {
        binding.tvNomeAlimento.setText(alimento.getNome());
        // Mostra a informação da porção base que vem do Firebase
        binding.tvPorcaoBase.setText(String.format(Locale.getDefault(), "Valores por %s", alimento.getPorcaoBase()));

        // Atualiza a UI com os valores para 0 unidades
        atualizarValoresNutricionais(0);
    }

    private void setupListeners() {
        // Listener para o campo de texto da quantidade
        binding.etQuantidadeUnidade.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    // Converte o texto digitado para um número
                    double quantidade = Double.parseDouble(s.toString());
                    atualizarValoresNutricionais(quantidade);
                } catch (NumberFormatException e) {
                    // Se o campo estiver vazio ou inválido (ex: "."), calcula com 0
                    atualizarValoresNutricionais(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener para o botão de adicionar
        binding.btnAdicionar.setOnClickListener(v -> {
            try {
                double quantidadeFinal = Double.parseDouble(binding.etQuantidadeUnidade.getText().toString());
                if (quantidadeFinal > 0) {
                    // Chama o método no modelo para calcular os nutrientes finais
                    alimento.calcularNutrientesPorPorcao(quantidadeFinal);
                    // Adiciona o alimento ao ViewModel
                    consultaViewModel.adicionarAlimento(alimento, tipoRefeicao);
                    dismiss(); // Fecha o dialog
                }
            } catch (NumberFormatException e) {
                // Não faz nada se o campo estiver vazio
            }
        });
    }

    private void atualizarValoresNutricionais(double quantidade) {
        // Chama o método de cálculo que você já tem na sua classe Alimento
        alimento.calcularNutrientesPorPorcao(quantidade);

        // Atualiza os TextViews com os valores calculados
        binding.tvKcalCalculado.setText(String.format(Locale.getDefault(), "Calorias: %.0f kcal", alimento.getEnergiaCalculada()));

        // Calcula o peso total para dar um feedback ao usuário
        double pesoTotal = quantidade * alimento.getPesoMedioG();
        binding.tvPesoTotal.setText(String.format(Locale.getDefault(), "Peso Total: %.0fg", pesoTotal));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpa a referência do binding para evitar memory leaks
    }
}
