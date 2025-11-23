package com.example.nutri3.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.nutri3.BuscarAlimentoFragment;
import com.example.nutri3.R;
import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.adapters.AlimentoSelecionado;
import com.example.nutri3.databinding.FragmentDietaBinding;
import com.example.nutri3.model.Alimento;
import com.example.nutri3.model.Avaliacao;
import com.example.nutri3.model.Dieta;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DietaFragment extends Fragment {

    private static final String TAG = "DietaFragment";
    private FragmentDietaBinding binding;
    private NavController navController;
    private ConsultaViewModel consultaViewModel;
    private String pacienteId;

    private AlimentoSelecionado cafeAdapter, almocoAdapter, cafeTardeAdapter, jantarAdapter, ceiaAdapter;

    public DietaFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDietaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        setupRecyclerViews();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        Dieta dieta = consultaViewModel.getDadosDieta().getValue();
        if (dieta == null) return;

        cafeAdapter = new AlimentoSelecionado(dieta.getCafeDaManha(), position -> {
            dieta.getCafeDaManha().remove(position);
            cafeAdapter.notifyItemRemoved(position);
            atualizarTotais();
        });
        binding.rvCafeManha.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCafeManha.setAdapter(cafeAdapter);

        almocoAdapter = new AlimentoSelecionado(dieta.getAlmoco(), position -> {
            dieta.getAlmoco().remove(position);
            almocoAdapter.notifyItemRemoved(position);
            atualizarTotais();
        });
        binding.rvAlmoco.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAlmoco.setAdapter(almocoAdapter);

        cafeTardeAdapter = new AlimentoSelecionado(dieta.getCafeDaTarde(), position -> {
            dieta.getCafeDaTarde().remove(position);
            cafeTardeAdapter.notifyItemRemoved(position);
            atualizarTotais();
        });
        binding.rvCafeTarde.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCafeTarde.setAdapter(cafeTardeAdapter);

        jantarAdapter = new AlimentoSelecionado(dieta.getJantar(), position -> {
            dieta.getJantar().remove(position);
            jantarAdapter.notifyItemRemoved(position);
            atualizarTotais();
        });
        binding.rvJantar.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvJantar.setAdapter(jantarAdapter);

        ceiaAdapter = new AlimentoSelecionado(dieta.getCeia(), position -> {
            dieta.getCeia().remove(position);
            ceiaAdapter.notifyItemRemoved(position);
            atualizarTotais();
        });
        binding.rvCeia.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCeia.setAdapter(ceiaAdapter);
    }

    private void setupClickListeners() {
        binding.btnVoltar.setOnClickListener(v -> navController.popBackStack());
        binding.btnSalvar.setOnClickListener(v -> finalizarESalvarConsulta());

        binding.btnAddCafeManha.setOnClickListener(v -> navegarParaBusca("cafeDaManha"));
        binding.btnAddAlmoco.setOnClickListener(v -> navegarParaBusca("almoco"));
        binding.btnAddCafeTarde.setOnClickListener(v -> navegarParaBusca("cafeDaTarde"));
        binding.btnAddJantar.setOnClickListener(v -> navegarParaBusca("jantar"));
        binding.btnAddCeia.setOnClickListener(v -> navegarParaBusca("ceia"));
    }

    private void navegarParaBusca(String tipoRefeicao) {
        BuscarAlimentoFragment dialog = BuscarAlimentoFragment.newInstance(tipoRefeicao);
        dialog.show(getParentFragmentManager(), "BuscarAlimentoFragment");
    }

    private void observeViewModel() {
        consultaViewModel.getPacienteIdSelecionado().observe(getViewLifecycleOwner(), id -> {
            if (id != null) this.pacienteId = id;
        });

        consultaViewModel.getDadosDieta().observe(getViewLifecycleOwner(), dieta -> {
            if (dieta != null) {
                if (cafeAdapter != null) cafeAdapter.notifyDataSetChanged();
                if (almocoAdapter != null) almocoAdapter.notifyDataSetChanged();
                if (cafeTardeAdapter != null) cafeTardeAdapter.notifyDataSetChanged();
                if (jantarAdapter != null) jantarAdapter.notifyDataSetChanged();
                if (ceiaAdapter != null) ceiaAdapter.notifyDataSetChanged();

                atualizarTotais();
            }
        });
    }

    private void atualizarTotais() {
        Dieta dieta = consultaViewModel.getDadosDieta().getValue();
        if (dieta == null) return;

        atualizarTotaisRefeicao(dieta.getCafeDaManha(), binding.tvTotaisCafeManha);
        atualizarTotaisRefeicao(dieta.getAlmoco(), binding.tvTotaisAlmoco);
        atualizarTotaisRefeicao(dieta.getCafeDaTarde(), binding.tvTotaisCafeTarde);
        atualizarTotaisRefeicao(dieta.getJantar(), binding.tvTotaisJantar);
        atualizarTotaisRefeicao(dieta.getCeia(), binding.tvTotaisCeia);

        List<Alimento> todosAlimentos = new ArrayList<>();
        todosAlimentos.addAll(dieta.getCafeDaManha());
        todosAlimentos.addAll(dieta.getAlmoco());
        todosAlimentos.addAll(dieta.getCafeDaTarde());
        todosAlimentos.addAll(dieta.getJantar());
        todosAlimentos.addAll(dieta.getCeia());
        atualizarTotaisRefeicao(todosAlimentos, binding.tvTotaisDia);
    }

    private void atualizarTotaisRefeicao(List<Alimento> alimentos, TextView textView) {
        double totalKcal = 0, totalCarb = 0, totalProt = 0, totalGord = 0;
        for (Alimento al : alimentos) {
            totalKcal += al.getEnergiaCalculada();
            totalCarb += al.getCarboidratosCalculado();
            totalProt += al.getProteinasCalculada();
            totalGord += al.getGordurasCalculada();
        }
        String totais = String.format(Locale.getDefault(),
                "Totais: %.0f Kcal | Carb %.1fg | Prot %.1fg | Gord %.1fg",
                totalKcal, totalCarb, totalProt, totalGord);
        textView.setText(totais);
    }

    private void finalizarESalvarConsulta() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || pacienteId == null) {
            Toast.makeText(getContext(), "Erro: Usuário ou paciente não identificado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Avaliacao avaliacao = consultaViewModel.getDadosAvaliacao().getValue();
        Dieta dieta = consultaViewModel.getDadosDieta().getValue();

        boolean isDietaPreenchida = dieta != null &&
                (!dieta.getCafeDaManha().isEmpty() || !dieta.getAlmoco().isEmpty() || !dieta.getCafeDaTarde().isEmpty() || !dieta.getJantar().isEmpty() || !dieta.getCeia().isEmpty());

        if (avaliacao == null && !isDietaPreenchida) {
            Toast.makeText(getContext(), "Nenhum dado para salvar.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSalvar.setEnabled(false);

        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference("pacientes")
                .child(currentUser.getUid()).child(pacienteId);

        List<Task<Void>> tasks = new ArrayList<>();

        if (avaliacao != null) {
            tasks.add(baseRef.child("avaliacoes").push().setValue(avaliacao));
        }
        if (isDietaPreenchida) {
            tasks.add(baseRef.child("dietas").push().setValue(dieta));
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(list -> {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Consulta salva com sucesso!", Toast.LENGTH_LONG).show();
            consultaViewModel.limparDados();
            navController.popBackStack(R.id.menu_home, false);
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSalvar.setEnabled(true);
            Toast.makeText(getContext(), "Falha ao salvar a consulta.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Erro ao salvar consulta no Firebase", e);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
