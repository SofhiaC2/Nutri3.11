package com.example.nutri3.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutri3.R;
import com.example.nutri3.databinding.ItemPacienteselectBinding;
import com.example.nutri3.databinding.ItemPacienteBinding;
import com.example.nutri3.fragments.consultas.Paciente; // Assumindo que seu modelo está em '.../model/Paciente'

import java.util.ArrayList;
import java.util.List;

public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.BaseViewHolder> implements Filterable {

    // --- MUDANÇA 1: Interface agora inclui o onSelectClick ---
    public interface OnPacienteInteractionListener {
        void onSelectClick(Paciente paciente); // Adicionado para a tela de seleção
        void onEditClick(Paciente paciente);
        void onDeleteClick(Paciente paciente);
    }

    private final List<Paciente> listaCompletaPacientes;
    private List<Paciente> listaFiltradaPacientes;
    private final OnPacienteInteractionListener listener;
    private final int layoutId; // Variável para guardar o ID do layout a ser usado

    // --- MUDANÇA 2: Construtor que recebe o ID do Layout ---
    public PacienteAdapter(List<Paciente> listaPacientes, int layoutId, OnPacienteInteractionListener listener) {
        this.listaCompletaPacientes = new ArrayList<>(listaPacientes);
        this.listaFiltradaPacientes = new ArrayList<>(listaPacientes);
        this.layoutId = layoutId; // Armazena o ID (R.layout.item_paciente ou R.layout.item_paciente2)
        this.listener = listener;
    }


    // Dentro da classe PacienteAdapter.java

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(this.layoutId, parent, false);

        // --- CORREÇÃO FINAL ---
        // Fazemos o "bind" diretamente na classe específica do Binding,
        // o que retorna o tipo correto sem a necessidade de conversões (casts)
        // ou ambiguidades para o compilador.
        if (this.layoutId == R.layout.item_paciente) {
            ItemPacienteBinding binding = ItemPacienteBinding.bind(view);
            return new PacienteNormalViewHolder(binding);
        } else {
            ItemPacienteselectBinding binding = ItemPacienteselectBinding.bind(view);
            return new PacienteSelectViewHolder(binding);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        Paciente pacienteAtual = listaFiltradaPacientes.get(position);
        holder.bind(pacienteAtual, listener);
    }

    @Override
    public int getItemCount() {
        return listaFiltradaPacientes.size();
    }

    // --- MUDANÇA 4: ViewHolder Base e Específicos ---

    // ViewHolder Base
    abstract static class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        abstract void bind(Paciente paciente, OnPacienteInteractionListener listener);
    }

    // ViewHolder para a tela NORMAL (com editar e excluir)
    static class PacienteNormalViewHolder extends BaseViewHolder {
        private final ItemPacienteBinding binding;

        public PacienteNormalViewHolder(ItemPacienteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        void bind(final Paciente paciente, final OnPacienteInteractionListener listener) {
            binding.tvNomePacienteItem.setText(paciente.getNome());
            binding.tvEmailPacienteItem.setText(paciente.getEmail());

            binding.ivEditarPaciente.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(paciente);
            });

            binding.ivExcluirPaciente.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(paciente);
            });
        }
    }

    // ViewHolder para a tela de SELEÇÃO
    static class PacienteSelectViewHolder extends BaseViewHolder {
        private final ItemPacienteselectBinding binding;

        public PacienteSelectViewHolder(ItemPacienteselectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        void bind(final Paciente paciente, final OnPacienteInteractionListener listener) {
            binding.tvNomePacienteItem.setText(paciente.getNome());
            binding.tvEmailPacienteItem.setText(paciente.getEmail());

            binding.btnSelecionar.setOnClickListener(v -> {
                if (listener != null) listener.onSelectClick(paciente);
            });
        }
    }

    // O código do filtro não precisa de alterações
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
            if (results.values != null) {
                listaFiltradaPacientes.addAll((List) results.values);
            }
            notifyDataSetChanged();
        }
    };

    // O método updateList também não precisa de alterações
    public void updateList(List<Paciente> novaLista) {
        this.listaCompletaPacientes.clear();
        this.listaCompletaPacientes.addAll(novaLista);
        this.listaFiltradaPacientes.clear();
        this.listaFiltradaPacientes.addAll(novaLista);
    }
}
