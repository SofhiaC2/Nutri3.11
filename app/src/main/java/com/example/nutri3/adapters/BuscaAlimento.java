package com.example.nutri3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutri3.R;
import com.example.nutri3.model.Alimento;
import java.util.List;
import java.util.Locale;

public class BuscaAlimento extends RecyclerView.Adapter<BuscaAlimento.BuscaViewHolder> {

    private final List<Alimento> resultados;
    private final OnAlimentoClickListener listener;

    public interface OnAlimentoClickListener {
        void onAlimentoClick(Alimento alimento);
    }

    public BuscaAlimento(List<Alimento> resultados, OnAlimentoClickListener listener) {
        this.resultados = resultados;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BuscaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alimento_busca, parent, false);
        return new BuscaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuscaViewHolder holder, int position) {
        Alimento alimento = resultados.get(position);
        holder.bind(alimento);
    }

    @Override
    public int getItemCount() {
        return resultados.size();
    }

    class BuscaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvDetalhes;

        BuscaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeAlimentoBusca);
            tvDetalhes = itemView.findViewById(R.id.tvDetalhesAlimentoBusca);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAlimentoClick(resultados.get(position));
                }
            });
        }

        void bind(Alimento alimento) {
            tvNome.setText(alimento.getNome());
            String detalhes = String.format(Locale.getDefault(),
                    "%.0f Kcal (100g)",
                    alimento.getEnergiaKcal()
            );
            tvDetalhes.setText(detalhes);
        }
    }
}
