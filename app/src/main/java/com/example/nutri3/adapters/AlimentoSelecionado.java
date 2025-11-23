package com.example.nutri3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutri3.R;
import com.example.nutri3.model.Alimento;
import java.util.List;
import java.util.Locale;

public class AlimentoSelecionado extends RecyclerView.Adapter<AlimentoSelecionado.AlimentoViewHolder> {

    private final List<Alimento> alimentos;
    private final OnAlimentoInteractionListener listener;

    public interface OnAlimentoInteractionListener {
        void onRemoveAlimento(int position);
    }

    public AlimentoSelecionado(List<Alimento> alimentos, OnAlimentoInteractionListener listener) {
        this.alimentos = alimentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlimentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alimento_selecionado, parent, false);
        return new AlimentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlimentoViewHolder holder, int position) {
        Alimento alimento = alimentos.get(position);
        holder.bind(alimento);
    }

    @Override
    public int getItemCount() {
        return alimentos.size();
    }

    class AlimentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeAlimento;
        TextView tvPorcao;
        TextView tvNutrientes;
        Button btnRemover;

        AlimentoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeAlimento = itemView.findViewById(R.id.tvNomeAlimento);
            tvPorcao = itemView.findViewById(R.id.tvPorcao);
            tvNutrientes = itemView.findViewById(R.id.tvNutrientes);
            btnRemover = itemView.findViewById(R.id.btnRemoverAlimento);

            btnRemover.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRemoveAlimento(position);
                }
            });
        }

        void bind(Alimento alimento) {
            tvNomeAlimento.setText(alimento.getNome());
            tvPorcao.setText(alimento.getDescricaoPorcao());

            String nutrientes = String.format(Locale.getDefault(),
                    "%.0f Kcal | C: %.1fg | P: %.1fg | G: %.1fg",
                    alimento.getEnergiaCalculada(),
                    alimento.getCarboidratosCalculado(),
                    alimento.getProteinasCalculada(),
                    alimento.getGordurasCalculada()
            );
            tvNutrientes.setText(nutrientes);
        }
    }
}
