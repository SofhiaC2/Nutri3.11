package com.example.nutri3.adapters; // Crie um pacote "adapters" se ainda não tiver

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutri3.databinding.ItemPacienteBinding; // Binding para item_paciente.xml
import com.example.nutri3.fragments.consultas.Paciente;


import java.util.ArrayList;
import java.util.List;

// Implementamos Filterable para habilitar a pesquisa
public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.PacienteViewHolder> implements Filterable {

    private final List<Paciente> listaCompletaPacientes; // Lista original, nunca modificada
    private List<Paciente> listaFiltradaPacientes;     // Lista exibida, que muda com a pesquisa

    // Construtor do Adapter
    public PacienteAdapter(List<Paciente> listaPacientes) {
        this.listaCompletaPacientes = new ArrayList<>(listaPacientes); // Cópia para segurança
        this.listaFiltradaPacientes = new ArrayList<>(listaPacientes); // A lista a ser exibida
    }

    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Cria a view para um item da lista usando View Binding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemPacienteBinding binding = ItemPacienteBinding.inflate(inflater, parent, false);
        return new PacienteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        // Pega o paciente da lista FILTRADA e preenche os dados na view
        Paciente pacienteAtual = listaFiltradaPacientes.get(position);
        holder.bind(pacienteAtual);
    }

    @Override
    public int getItemCount() {
        // Retorna o tamanho da lista FILTRADA
        return listaFiltradaPacientes.size();
    }

    // Classe interna ViewHolder que segura as referências das views de um item
    static class PacienteViewHolder extends RecyclerView.ViewHolder {
        private final ItemPacienteBinding binding;

        public PacienteViewHolder(ItemPacienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Paciente paciente) {
            binding.tvNomePacienteItem.setText(paciente.getNome());
            binding.tvEmailPacienteItem.setText(paciente.getEmail());
            // Você pode adicionar mais lógica aqui, como carregar uma imagem, etc.
            // Ex: binding.ivIconePaciente.setImageResource(...);
        }
    }

    // --- LÓGICA DE FILTRAGEM (PESQUISA) ---
    @Override
    public Filter getFilter() {
        return filtroPaciente;
    }

    private final Filter filtroPaciente = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Paciente> listaSugerida = new ArrayList<>();

            // Se a barra de pesquisa estiver vazia, mostra a lista completa
            if (constraint == null || constraint.length() == 0) {
                listaSugerida.addAll(listaCompletaPacientes);
            } else {
                // Converte o texto da pesquisa para minúsculas para uma busca case-insensitive
                String filterPattern = constraint.toString().toLowerCase().trim();

                // Percorre a lista completa
                for (Paciente item : listaCompletaPacientes) {
                    // Se o nome do paciente contiver o texto pesquisado
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
            // Limpa a lista atual e adiciona os resultados filtrados
            listaFiltradaPacientes.clear();
            listaFiltradaPacientes.addAll((List) results.values);
            // Notifica o adapter que os dados mudaram, para redesenhar a lista
            notifyDataSetChanged();
        }
    };
}
