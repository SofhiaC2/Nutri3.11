package com.example.nutri3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.adapters.BuscaAlimento;
import com.example.nutri3.databinding.DialogBuscarAlimentoBinding;
import com.example.nutri3.model.Alimento;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BuscarAlimentoFragment extends DialogFragment {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String TAG = "BuscarAlimento";
    private static final String ARG_TIPO_REFEICAO = "tipo_refeicao";

    private DialogBuscarAlimentoBinding binding;
    private ConsultaViewModel consultaViewModel;
    private BuscaAlimento adapter;
    private String tipoRefeicao;

    private final List<Alimento> listaCompletaAlimentos = new ArrayList<>();
    private final List<Alimento> resultadosBusca = new ArrayList<>();

    public static BuscarAlimentoFragment newInstance(String tipoRefeicao) {
        BuscarAlimentoFragment fragment = new BuscarAlimentoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TIPO_REFEICAO, tipoRefeicao);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tipoRefeicao = getArguments().getString(ARG_TIPO_REFEICAO);
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Nutri3_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogBuscarAlimentoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        consultaViewModel = new ViewModelProvider(requireActivity()).get(ConsultaViewModel.class);

        setupRecyclerView();
        setupListeners();
        carregarListaCompletaDeAlimentos();
    }

    private void setupRecyclerView() {
        adapter = new BuscaAlimento(resultadosBusca, alimento -> {
            QuantidadeAlimentoFragment dialogQuantidade = QuantidadeAlimentoFragment.newInstance(alimento, tipoRefeicao);
            dialogQuantidade.show(getParentFragmentManager(), "QuantidadeAlimentoFragment");
            dismiss();
        });
        binding.rvResultadosBusca.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvResultadosBusca.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnFecharBusca.setOnClickListener(v -> dismiss());

        binding.etBuscaAlimento.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarAlimentosLocalmente(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void carregarListaCompletaDeAlimentos() {
        binding.progressBarBusca.setVisibility(View.VISIBLE);
        Log.d(TAG, "Iniciando carregamento do Firebase...");

        DatabaseReference alimentosRef = FirebaseDatabase.getInstance().getReference("alimentos");

        alimentosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                if (!snapshot.exists()) {
                    binding.progressBarBusca.setVisibility(View.GONE);
                    Log.e(TAG, "O snapshot em /alimentos não existe!");
                    return;
                }

                executor.execute(() -> {
                    List<Alimento> alimentosCarregados = new ArrayList<>();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Alimento alimento = data.getValue(Alimento.class);
                        if (alimento != null && alimento.getNome() != null) {
                            alimentosCarregados.add(alimento);
                        }
                    }
                    Log.d(TAG, "Processamento em background finalizado. Total: " + alimentosCarregados.size());

                    handler.post(() -> {
                        if (!isAdded()) return;

                        listaCompletaAlimentos.clear();
                        listaCompletaAlimentos.addAll(alimentosCarregados);

                        binding.progressBarBusca.setVisibility(View.GONE);
                        Log.d(TAG, "Lista principal atualizada na UI Thread.");

                        filtrarAlimentosLocalmente(binding.etBuscaAlimento.getText().toString());
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                binding.progressBarBusca.setVisibility(View.GONE);
                Log.e(TAG, "Erro ao carregar lista do Firebase.", error.toException());
            }
        });
    }

    private void filtrarAlimentosLocalmente(String textoBusca) {
        resultadosBusca.clear();

        if (!textoBusca.isEmpty() && !listaCompletaAlimentos.isEmpty()) {
            String textoBuscaLower = textoBusca.toLowerCase(Locale.ROOT);
            for (Alimento alimento : listaCompletaAlimentos) {
                if (alimento.getNome().toLowerCase(Locale.ROOT).contains(textoBuscaLower)) {
                    resultadosBusca.add(alimento);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
