package com.example.nutri3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutri3.R;
import com.example.nutri3.model.Alimento;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlimentoSelecionado extends RecyclerView.Adapter<AlimentoSelecionado.AlimentoViewHolder> {

    private List<Alimento> alimentos = new ArrayList<>();

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

    public void setAlimentos(List<Alimento> novosAlimentos) {
        this.alimentos = novosAlimentos;
        notifyDataSetChanged();
    }

    // --- CORREÇÃO ESTÁ AQUI ---
    static class AlimentoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvPorcao; // Referência para o TextView da porção
        TextView tvNutrientes; // Referência para o TextView dos nutrientes

        public AlimentoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Faz o findViewById para os IDs do SEU layout
            tvNome = itemView.findViewById(R.id.tvNomeAlimento);
            tvPorcao = itemView.findViewById(R.id.tvPorcao);
            tvNutrientes = itemView.findViewById(R.id.tvNutrientes);
            // O botão de remover pode ser implementado depois
        }

        void bind(Alimento alimento) {
            tvNome.setText(alimento.getNome());

            // Usa os valores CALCULADOS que vieram do Alimento.java

            // 1. Preenche o TextView da Porção
            // O método getDescricaoPorcao() já cria o texto formatado "X unidades (Yg)" ou "Zg"
            tvPorcao.setText(alimento.getDescricaoPorcao());

            // 2. Preenche o TextView dos Nutrientes
            String nutrientesFormatados = String.format(Locale.getDefault(),
                    "%.0f Kcal | C: %.1fg | P: %.1fg | G: %.1fg",
                    alimento.getEnergiaCalculada(),
                    alimento.getCarboidratosCalculado(),
                    alimento.getProteinasCalculada(),
                    alimento.getGordurasCalculada());
            tvNutrientes.setText(nutrientesFormatados);
        }
    }
}
