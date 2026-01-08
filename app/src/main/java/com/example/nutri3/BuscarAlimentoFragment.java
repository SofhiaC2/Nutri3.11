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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.nutri3.DialogQuantidadeG;

import com.example.nutri3.ViewModel.ConsultaViewModel;
import com.example.nutri3.adapters.BuscaAlimento;
import com.example.nutri3.databinding.DialogBuscarAlimentoBinding;
import com.example.nutri3.model.Alimento;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Locale;

public class BuscarAlimentoFragment extends DialogFragment {

    private static final String TAG = "BuscarAlimento";
    private static final String ARG_TIPO_REFEICAO = "tipo_refeicao";

    private DialogBuscarAlimentoBinding binding;
    private ConsultaViewModel consultaViewModel;
    private BuscaAlimento adapter;
    private String tipoRefeicao;

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
        // Inicia com uma query vazia.
        updateSearchQuery("");
    }

    private void setupRecyclerView() {
        binding.rvResultadosBusca.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        binding.btnFecharBusca.setOnClickListener(v -> dismiss());

        binding.etBuscaAlimento.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateSearchQuery(String searchText) {
        DatabaseReference alimentosRef =
                FirebaseDatabase.getInstance().getReference("alimentos");

        String textoBuscaNormalizado = searchText
                .toLowerCase(Locale.ROOT)
                .trim();

        // 🔒 Só busca com 2 ou mais caracteres
        if (textoBuscaNormalizado.length() < 2) {
            // Lógica para limpar a busca...
            if (adapter != null) {
                adapter.stopListening();
                binding.rvResultadosBusca.setAdapter(null);
                adapter = null;
            }
            binding.progressBarBusca.setVisibility(View.GONE);
            binding.tvNenhumResultado.setVisibility(View.GONE);
            return;
        }

        // 🔍 Busca real
        binding.progressBarBusca.setVisibility(View.VISIBLE);
        binding.tvNenhumResultado.setVisibility(View.GONE);

        Query query = alimentosRef
                .orderByChild("nome_normalizado")
                .startAt(textoBuscaNormalizado)
                .endAt(textoBuscaNormalizado + "\uf8ff")
                .limitToFirst(50);

        FirebaseRecyclerOptions<Alimento> options =
                new FirebaseRecyclerOptions.Builder<Alimento>()
                        .setQuery(query, Alimento.class)
                        .build();

        // Se o adapter for nulo, cria um novo
        if (adapter == null) {
            // AQUI ESTÁ A LÓGICA PRINCIPAL COM SEUS NOMES DE ARQUIVO
            adapter = new BuscaAlimento(options, alimento -> {
                // Verificamos o booleano 'por_unidade'
                if (alimento.isPorUnidade()) {

                    DialogQuantidadeU dialog = DialogQuantidadeU.newInstance(alimento, tipoRefeicao);
                    dialog.show(getParentFragmentManager(), "DialogUnidade");
                } else {

                    DialogQuantidadeG dialog = DialogQuantidadeG.newInstance(alimento, tipoRefeicao);
                    dialog.show(getParentFragmentManager(), "DialogGrama");
                }
                // Não fechamos o dialog de busca, permitindo adicionar vários alimentos
            }) {
                @Override
                public void onDataChanged() {
                    super.onDataChanged();
                    binding.progressBarBusca.setVisibility(View.GONE);
                    if (getItemCount() == 0) {
                        binding.tvNenhumResultado.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNenhumResultado.setVisibility(View.GONE);
                    }
                }
            };

            binding.rvResultadosBusca.setAdapter(adapter);
            adapter.startListening();
        } else {
            // Se já existe, apenas atualiza a query e reinicia o 'listening'
            adapter.stopListening();
            adapter.updateOptions(options);
            adapter.startListening();
        }
    }

    // <-- OS MÉTODOS onStart e onStop FORAM MOVIDOS PARA CÁ, FORA DO updateSearchQuery -->
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
