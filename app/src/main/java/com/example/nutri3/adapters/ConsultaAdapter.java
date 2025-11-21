package com.example.nutri3.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutri3.R;
import com.google.firebase.database.DataSnapshot;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConsultaAdapter extends RecyclerView.Adapter<ConsultaAdapter.ConsultaViewHolder> {

    private final Context context;
    private final List<DataSnapshot> consultaList;
    private final OnConsultaListener listener;

    // Interface para comunicar eventos de clique para o Fragment
    public interface OnConsultaListener {
        void onConsultaDelete(String consultaId, String nomePaciente);
    }

    public ConsultaAdapter(Context context, List<DataSnapshot> consultaList, OnConsultaListener listener) {
        this.context = context;
        this.consultaList = consultaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConsultaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_consulta, parent, false);
        return new ConsultaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultaViewHolder holder, int position) {
        DataSnapshot snapshot = consultaList.get(position);
        String nome = snapshot.child("nomePaciente").getValue(String.class);
        Long timestamp = snapshot.child("timestamp").getValue(Long.class);
        String consultaId = snapshot.getKey();

        if (nome != null && timestamp != null) {
            SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String infoText = "• " + nome + "\n  Data: " + sdfData.format(timestamp) + "  Hora: " + sdfHora.format(timestamp);
            holder.tvInfo.setText(infoText);
        }

        holder.ivDelete.setOnClickListener(v -> {
            if (consultaId != null && listener != null) {
                listener.onConsultaDelete(consultaId, nome);
            }
        });
    }

    @Override
    public int getItemCount() {
        return consultaList.size();
    }

    static class ConsultaViewHolder extends RecyclerView.ViewHolder {
        TextView tvInfo;
        ImageView ivDelete;

        public ConsultaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInfo = itemView.findViewById(R.id.tv_info_consulta_item);
            ivDelete = itemView.findViewById(R.id.iv_delete_consulta);
        }
    }
}
