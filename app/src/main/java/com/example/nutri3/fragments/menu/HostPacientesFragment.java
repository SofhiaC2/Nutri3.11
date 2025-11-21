package com.example.nutri3.fragments.menu;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentPacienteshostBinding;
import com.example.nutri3.ViewModel.ConsultaViewModel;

public class HostPacientesFragment extends Fragment {

    private FragmentPacienteshostBinding binding;
    private NavController navController;

    private ConsultaViewModel consultaViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPacienteshostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);

        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnPacientesVoltar.setOnClickListener(v -> {
            navController.popBackStack();
        });

        binding.btnMenuAdicionarPaciente.setOnClickListener(v -> {
            navController.navigate(R.id.action_hostPacientesFragment2_to_adicionarPacientesFragment2);
        });

        binding.btnMenuVerPaciente.setOnClickListener(v -> {
            navController.navigate(R.id.action_hostPacientesFragment2_to_verPacientesFragment);
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
