package com.example.nutri3.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutri3.BuscarAlimentoFragment;
import com.example.nutri3.R; // Import necessário para o R.layout
import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.adapters.AlimentoSelecionado;
import com.example.nutri3.databinding.FragmentDietaBinding;

import java.util.Locale;

public class DietaFragment extends Fragment {

    private FragmentDietaBinding binding;
    private ConsultaViewModel consultaViewModel;

    private AlimentoSelecionado cafeAdapter;
    private AlimentoSelecionado almocoAdapter;
    private AlimentoSelecionado cafeDaTardeAdapter;
    private AlimentoSelecionado jantarAdapter;
    private AlimentoSelecionado ceiaAdapter;

    // Adicione esta linha para inflar o layout corretamente
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDietaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // A linha "binding = FragmentDietaBinding.bind(view);" foi movida para o onCreateView

        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        setupRecyclerViews();
        setupClickListeners(); // Separando a lógica dos cliques
        observeViewModel();
    }

    private void setupRecyclerViews() {
        // --- IDs CORRIGIDOS AQUI ---

        // Café da Manhã
        binding.rvCafeManha.setLayoutManager(new LinearLayoutManager(getContext()));
        cafeAdapter = new AlimentoSelecionado();
        binding.rvCafeManha.setAdapter(cafeAdapter);

        // Almoço
        binding.rvAlmoco.setLayoutManager(new LinearLayoutManager(getContext()));
        almocoAdapter = new AlimentoSelecionado();
        binding.rvAlmoco.setAdapter(almocoAdapter);

        // Café da Tarde
        binding.rvCafeTarde.setLayoutManager(new LinearLayoutManager(getContext()));
        cafeDaTardeAdapter = new AlimentoSelecionado();
        binding.rvCafeTarde.setAdapter(cafeDaTardeAdapter);

        // Jantar
        binding.rvJantar.setLayoutManager(new LinearLayoutManager(getContext()));
        jantarAdapter = new AlimentoSelecionado();
        binding.rvJantar.setAdapter(jantarAdapter);

        // Ceia
        binding.rvCeia.setLayoutManager(new LinearLayoutManager(getContext()));
        ceiaAdapter = new AlimentoSelecionado();
        binding.rvCeia.setAdapter(ceiaAdapter);
    }

    private void setupClickListeners() {
        // --- IDs DOS BOTÕES CORRIGIDOS AQUI ---
        binding.btnAddCafeManha.setOnClickListener(v -> {
            // O nome da refeição deve bater com o `case` no ViewModel
            BuscarAlimentoFragment dialog = BuscarAlimentoFragment.newInstance("cafeDaManha");
            dialog.show(getParentFragmentManager(), "BuscarAlimento");
        });

        binding.btnAddAlmoco.setOnClickListener(v -> {
            BuscarAlimentoFragment dialog = BuscarAlimentoFragment.newInstance("almoco");
            dialog.show(getParentFragmentManager(), "BuscarAlimento");
        });

        binding.btnAddCafeTarde.setOnClickListener(v -> {
            BuscarAlimentoFragment dialog = BuscarAlimentoFragment.newInstance("cafeDaTarde");
            dialog.show(getParentFragmentManager(), "BuscarAlimento");
        });

        binding.btnAddJantar.setOnClickListener(v -> {
            BuscarAlimentoFragment dialog = BuscarAlimentoFragment.newInstance("jantar");
            dialog.show(getParentFragmentManager(), "BuscarAlimento");
        });

        binding.btnAddCeia.setOnClickListener(v -> {
            BuscarAlimentoFragment dialog = BuscarAlimentoFragment.newInstance("ceia");
            dialog.show(getParentFragmentManager(), "BuscarAlimento");
        });
    }


    private void observeViewModel() {
        consultaViewModel.getDadosDieta().observe(getViewLifecycleOwner(), dieta -> {
            if (dieta != null) {
                // Atualiza as listas dos adapters
                cafeAdapter.setAlimentos(dieta.getCafeDaManha());
                almocoAdapter.setAlimentos(dieta.getAlmoco());
                cafeDaTardeAdapter.setAlimentos(dieta.getCafeDaTarde());
                jantarAdapter.setAlimentos(dieta.getJantar());
                ceiaAdapter.setAlimentos(dieta.getCeia());

                // --- LÓGICA PARA ATUALIZAR OS TOTAIS (BÔNUS) ---
                atualizarTotais();
            }
        });
    }

    private void atualizarTotais() {
        if (consultaViewModel == null || consultaViewModel.getDadosDieta().getValue() == null) return;

        double totalKcal = 0, totalCarb = 0, totalProt = 0, totalGord = 0;
        double kcalCafe = 0, carbCafe = 0, protCafe = 0, gordCafe = 0;
        // ... (variáveis para outras refeições)

        for(com.example.nutri3.model.Alimento al : consultaViewModel.getDadosDieta().getValue().getCafeDaManha()) {
            kcalCafe += al.getEnergiaCalculada();
            carbCafe += al.getCarboidratosCalculado();
            protCafe += al.getProteinasCalculada();
            gordCafe += al.getGordurasCalculada();
        }
        binding.tvTotaisCafeManha.setText(String.format(Locale.getDefault(), "Totais: Kcal %.0f | Carb %.1fg | Prot %.1fg | Gord %.1fg", kcalCafe, carbCafe, protCafe, gordCafe));
        totalKcal += kcalCafe; totalCarb += carbCafe; totalProt += protCafe; totalGord += gordCafe;

        // Repita o bloco acima para Almoço, Jantar, etc., usando os TextViews corretos (tvTotaisAlmoco, tvTotaisJantar...)

        // Atualiza o total do dia
        binding.tvTotaisDia.setText(String.format(Locale.getDefault(), "Totais: Kcal %.0f | Carb %.1fg | Prot %.1fg | Gord %.1fg", totalKcal, totalCarb, totalProt, totalGord));
    }


    // Adicione esta linha para limpar a referência do binding e evitar memory leaks
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
