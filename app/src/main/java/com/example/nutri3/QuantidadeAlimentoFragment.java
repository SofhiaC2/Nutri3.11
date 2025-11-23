package com.example.nutri3;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.databinding.DialogQuantidadeAlimentoBinding;
import com.example.nutri3.model.Alimento;

public class QuantidadeAlimentoFragment extends DialogFragment {

    private static final String ARG_ALIMENTO = "alimento";
    private static final String ARG_TIPO_REFEICAO = "tipo_refeicao";

    private DialogQuantidadeAlimentoBinding binding;
    private ConsultaViewModel consultaViewModel;
    private Alimento alimento;
    private String tipoRefeicao;

    public static QuantidadeAlimentoFragment newInstance(Alimento alimento, String tipoRefeicao) {
        QuantidadeAlimentoFragment fragment = new QuantidadeAlimentoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALIMENTO, alimento);
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogQuantidadeAlimentoBinding.inflate(inflater, container, false);
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

        binding.tvNomeAlimentoQuantidade.setText(alimento.getNome());
        if (alimento.isPorUnidade()) {
            binding.tvInstrucao.setText(String.format("Informe a quantidade em unidades (1 unidade ≈ %.0fg):", alimento.getPesoMedioG()));
            binding.etQuantidade.setHint("Ex: 2");
        } else {
            binding.tvInstrucao.setText("Informe a quantidade em gramas (g):");
            binding.etQuantidade.setHint("Ex: 150");
        }

        binding.btnConfirmarQuantidade.setOnClickListener(v -> {
            String quantidadeStr = binding.etQuantidade.getText().toString();
            if (TextUtils.isEmpty(quantidadeStr)) {
                Toast.makeText(getContext(), "Por favor, informe a quantidade.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantidade = Double.parseDouble(quantidadeStr);
                alimento.calcularNutrientesPorPorcao(quantidade);
                consultaViewModel.adicionarAlimento(alimento, tipoRefeicao);
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Quantidade inválida.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
