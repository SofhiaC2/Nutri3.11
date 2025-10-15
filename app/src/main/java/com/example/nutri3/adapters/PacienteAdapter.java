package com.example.nutri3.adapters;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView; // Import necessário
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation; // Import necessário
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutri3.R; // Import necessário
import com.example.nutri3.databinding.ItemPacienteBinding;
import com.example.nutri3.fragments.consultas.Paciente; // Importe seu modelo Paciente
import com.example.nutri3.fragments.consultas.VerPacientesFragmentDirections; // Import da classe gerada pelo Safe Args
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.PacienteViewHolder> implements Filterable {

    // --- MUDANÇA 1: Interface para comunicar cliques ao Fragment ---
    public interface OnPacienteInteractionListener {
        void onEditClick(Paciente paciente);
        void onDeleteClick(Paciente paciente);
    }

    private final List<Paciente> listaCompletaPacientes;
    private List<Paciente> listaFiltradaPacientes;
    private final OnPacienteInteractionListener listener; // Armazena a referência ao listener



    public void updateList(List<Paciente> novaLista) {
        // Atualiza a lista completa que é usada como base para a filtragem
        this.listaCompletaPacientes.clear();
        this.listaCompletaPacientes.addAll(novaLista);

        // A lista filtrada (que é a exibida) também é atualizada.
        // O filtro será reaplicado pelo Fragment, se houver texto na busca.
        this.listaFiltradaPacientes.clear();
        this.listaFiltradaPacientes.addAll(novaLista);
    }



    public PacienteAdapter(List<Paciente> listaPacientes, OnPacienteInteractionListener listener) {
        this.listaCompletaPacientes = new ArrayList<>(listaPacientes);
        this.listaFiltradaPacientes = new ArrayList<>(listaPacientes);
        this.listener = listener; // Atribui o listener
    }

    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPacienteBinding binding = ItemPacienteBinding.inflate(inflater, parent, false);
        return new PacienteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        Paciente pacienteAtual = listaFiltradaPacientes.get(position);
        // --- MUDANÇA 3: Passa o paciente e o listener para o ViewHolder ---
        holder.bind(pacienteAtual, listener);
    }

    @Override
    public int getItemCount() {
        return listaFiltradaPacientes.size();
    }

    // --- MUDANÇA 4: ViewHolder agora configura os cliques ---
    static class PacienteViewHolder extends RecyclerView.ViewHolder {
        private final ItemPacienteBinding binding;

        public PacienteViewHolder(ItemPacienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Paciente paciente, final OnPacienteInteractionListener listener) {
            binding.tvNomePacienteItem.setText(paciente.getNome());
            binding.tvEmailPacienteItem.setText(paciente.getEmail());

            // Configura o clique no ícone de EDITAR
            binding.ivEditarPaciente.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(paciente);
                }
            });

            // Configura o clique no ícone de EXCLUIR
            binding.ivExcluirPaciente.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(paciente);
                }
            });
        }
    }

    @Override
    public Filter getFilter() {
        return filtroPaciente;
    }

    private final Filter filtroPaciente = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Paciente> listaSugerida = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                listaSugerida.addAll(listaCompletaPacientes);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Paciente item : listaCompletaPacientes) {
                    if (item.getNome().toLowerCase().contains(filterPattern)) {
                        listaSugerida.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = listaSugerida;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            listaFiltradaPacientes.clear();
            listaFiltradaPacientes.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
