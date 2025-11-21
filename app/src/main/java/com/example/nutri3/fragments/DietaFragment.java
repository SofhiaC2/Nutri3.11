
package com.example.nutri3.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentDietaBinding;
import com.example.nutri3.ViewModel.ConsultaViewModel;

public class DietaFragment extends Fragment {

    private FragmentDietaBinding binding;
    private NavController navController;
    private ConsultaViewModel consultaViewModel;

    public DietaFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDietaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        setupClickListeners();

        observeViewModel();
    }

    private void observeViewModel() {
        consultaViewModel.getAvaliacao().observe(getViewLifecycleOwner(), avaliacao -> {
            if (avaliacao != null && avaliacao.getAltura() > 0) {
                double imc = avaliacao.getPeso() / (avaliacao.getAltura() * avaliacao.getAltura());
                String imcFormatado = String.format("%.2f", imc);
                Toast.makeText(getContext(), "Dados da avaliação recebidos! IMC calculado: " + imcFormatado, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnVoltar.setOnClickListener(v -> {
            navController.popBackStack();
        });

        binding.btnAddCafeManha.setOnClickListener(v -> {
            finalizarConsulta();
        });

        binding.btnAddAlmoco.setOnClickListener(v -> Toast.makeText(getContext(), "Adicionar Almoço: A implementar.", Toast.LENGTH_SHORT).show());
        binding.btnAddCafeTarde.setOnClickListener(v -> Toast.makeText(getContext(), "Adicionar Café da Tarde: A implementar.", Toast.LENGTH_SHORT).show());
        binding.btnAddJantar.setOnClickListener(v -> Toast.makeText(getContext(), "Adicionar Jantar: A implementar.", Toast.LENGTH_SHORT).show());
        binding.btnAddCeia.setOnClickListener(v -> Toast.makeText(getContext(), "Adicionar Ceia: A implementar.", Toast.LENGTH_SHORT).show());
    }

    private void finalizarConsulta() {

        Toast.makeText(getContext(), "Consulta completa salva com sucesso!", Toast.LENGTH_LONG).show();

        consultaViewModel.limparDados();

        navController.popBackStack(R.id.hostPacientesFragment2, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
