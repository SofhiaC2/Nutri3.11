package com.example.nutri3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Importar
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutri3.R;
import com.example.nutri3.model.Alimento;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.Locale;

public class BuscaAlimento extends FirebaseRecyclerAdapter<Alimento, BuscaAlimento.BuscaViewHolder> {

    private final OnAddAlimentoListener listener;

    // 1. Interface simplificada para a ÚNICA AÇÃO que queremos: adicionar
    public interface OnAddAlimentoListener {
        void onAddClick(Alimento alimento);
    }

    public BuscaAlimento(@NonNull FirebaseRecyclerOptions<Alimento> options, OnAddAlimentoListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull BuscaViewHolder holder, int position, @NonNull Alimento model) {
        holder.bind(model);
        // 2. O clique agora é no botão, não no item inteiro
        holder.btnAdd.setOnClickListener(v -> {
            listener.onAddClick(model);
        });
    }

    @NonNull
    @Override
    public BuscaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alimento_busca, parent, false);
        return new BuscaViewHolder(view);
    }

    public static class BuscaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvDetalhes;
        ImageButton btnAdd; // 3. Referência para o novo botão

        public BuscaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeAlimentoBusca);
            tvDetalhes = itemView.findViewById(R.id.tvDetalhesAlimentoBusca);
            btnAdd = itemView.findViewById(R.id.btnAdicionarAlimen); // 4. Pegar o novo botão
        }

        void bind(Alimento alimento) {
            tvNome.setText(alimento.getNome());
            String porcao = alimento.getPorcaoBase() != null ? alimento.getPorcaoBase() : "100g";
            String detalhes = String.format(Locale.getDefault(),
                    "%.0f Kcal (%s)",
                    alimento.getEnergiaKcal(),
                    porcao
            );
            tvDetalhes.setText(detalhes);
        }
    }
}
