package com.example.nutri3.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

// Adicione os imports explicitamente para garantir que o IDE os encontre
import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentAvaliacaoBinding;
import com.example.nutri3.model.Avaliacao;
import com.example.nutri3.ViewModel.ConsultaViewModel; // Se não houver pacote, este é o import correto

public class AvaliacaoFragment extends Fragment {

    private FragmentAvaliacaoBinding binding;
    private NavController navController;
    private ConsultaViewModel consultaViewModel;

    public AvaliacaoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAvaliacaoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        consultaViewModel.getAvaliacao().observe(getViewLifecycleOwner(), avaliacao -> {
            if (avaliacao != null) {
                // Preenche TODOS os campos com os dados do ViewModel.
                if (avaliacao.getPeso() > 0) binding.etPeso.setText(String.valueOf(avaliacao.getPeso()));
                if (avaliacao.getAltura() > 0) binding.etAltura.setText(String.valueOf(avaliacao.getAltura()));
                if (avaliacao.getCintura() > 0) binding.etCintura.setText(String.valueOf(avaliacao.getCintura()));
                if (avaliacao.getQuadril() > 0) binding.etQuadril.setText(String.valueOf(avaliacao.getQuadril()));
                if (avaliacao.getBraco() > 0) binding.etBraco.setText(String.valueOf(avaliacao.getBraco()));
                if (avaliacao.getCoxa() > 0) binding.etCoxa.setText(String.valueOf(avaliacao.getCoxa()));
                if (avaliacao.getPanturrilha() > 0) binding.etPanturrilha.setText(String.valueOf(avaliacao.getPanturrilha()));
                if (avaliacao.getTriceps() > 0) binding.etTriceps.setText(String.valueOf(avaliacao.getTriceps()));
                if (avaliacao.getSubescapular() > 0) binding.etSubescapular.setText(String.valueOf(avaliacao.getSubescapular()));
                if (avaliacao.getSuprailiaca() > 0) binding.etSuprailiaca.setText(String.valueOf(avaliacao.getSuprailiaca()));
                if (avaliacao.getAbdominal() > 0) binding.etAbdominal.setText(String.valueOf(avaliacao.getAbdominal()));
                if (avaliacao.getCoxaDobras() > 0) binding.etCoxaDobras.setText(String.valueOf(avaliacao.getCoxaDobras()));
            }
        });
    }

    private void setupClickListeners() {
        binding.btnSalvarAvaliacao.setOnClickListener(v -> {
            // A validação agora acontece dentro do salvarDadosNoViewModel,
            // mas só para o clique do botão de salvar, não para o de voltar.
            if (!validarEsalvarDados(true)) {
                return; // Falha na validação, para a execução
            }

            Boolean dietaSelecionada = consultaViewModel.getIsDietaSelecionada().getValue();
            if (dietaSelecionada != null && dietaSelecionada) {
                Toast.makeText(getContext(), "Avaliação salva. Próximo passo: Plano Alimentar.", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_avaliacaoFragment_to_dietaFragment);
            } else {
                Toast.makeText(getContext(), "Avaliação salva com sucesso!", Toast.LENGTH_SHORT).show();
                // TODO: Salvar os dados do ViewModel no Firebase aqui
                consultaViewModel.limparDados();
                navController.popBackStack(R.id.hostPacientesFragment, false);
            }
        });

        binding.btnVoltar.setOnClickListener(v -> {
            // Apenas salva os dados, sem validar campos obrigatórios
            validarEsalvarDados(false);
            navController.popBackStack();
        });
    }

    // --- MÉTODO ATUALIZADO ---
    private boolean validarEsalvarDados(boolean validarObrigatorios) {
        if (validarObrigatorios) {
            if (TextUtils.isEmpty(binding.etPeso.getText()) || TextUtils.isEmpty(binding.etAltura.getText())) {
                Toast.makeText(getContext(), "Peso e Altura são obrigatórios.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        Avaliacao dadosAtuais = new Avaliacao();
        try {
            // Usa o método auxiliar para todos os campos
            dadosAtuais.setPeso(parseDoubleOrZero(binding.etPeso.getText().toString()));
            dadosAtuais.setAltura(parseDoubleOrZero(binding.etAltura.getText().toString()));
            dadosAtuais.setCintura(parseDoubleOrZero(binding.etCintura.getText().toString()));
            dadosAtuais.setQuadril(parseDoubleOrZero(binding.etQuadril.getText().toString()));
            dadosAtuais.setBraco(parseDoubleOrZero(binding.etBraco.getText().toString()));
            dadosAtuais.setCoxa(parseDoubleOrZero(binding.etCoxa.getText().toString()));
            dadosAtuais.setPanturrilha(parseDoubleOrZero(binding.etPanturrilha.getText().toString()));
            dadosAtuais.setTriceps(parseDoubleOrZero(binding.etTriceps.getText().toString()));
            dadosAtuais.setSubescapular(parseDoubleOrZero(binding.etSubescapular.getText().toString()));
            dadosAtuais.setSuprailiaca(parseDoubleOrZero(binding.etSuprailiaca.getText().toString()));
            dadosAtuais.setAbdominal(parseDoubleOrZero(binding.etAbdominal.getText().toString()));
            dadosAtuais.setCoxaDobras(parseDoubleOrZero(binding.etCoxaDobras.getText().toString()));

        } catch (Exception e) { // Captura qualquer erro inesperado
            Toast.makeText(getContext(), "Erro ao processar os dados.", Toast.LENGTH_SHORT).show();
            return false;
        }

        consultaViewModel.setAvaliacao(dadosAtuais);
        return true;
    }

    // --- NOVO MÉTODO HELPER ---
    private double parseDoubleOrZero(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace(',', '.')); // Substitui vírgula por ponto
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
