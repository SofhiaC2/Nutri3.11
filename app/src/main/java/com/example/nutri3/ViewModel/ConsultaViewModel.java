package com.example.nutri3.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// --- INÍCIO DA CORREÇÃO ---
// Importe o MODELO, não o FRAGMENTO
import com.example.nutri3.model.Avaliacao;
// --- FIM DA CORREÇÃO ---

public class ConsultaViewModel extends ViewModel {

    // --- INÍCIO DA CORREÇÃO ---
    // O LiveData deve guardar um objeto do tipo "Avaliacao"
    private final MutableLiveData<Avaliacao> avaliacaoData = new MutableLiveData<>();
    // --- FIM DA CORREÇÃO ---

    // Guarda as seleções do diálogo inicial
    private final MutableLiveData<Boolean> isAvaliacaoSelecionada = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDietaSelecionada = new MutableLiveData<>(false);

    // --- MÉTODOS PARA AVALIAÇÃO (AGORA CORRIGIDOS) ---
    public void setAvaliacao(Avaliacao avaliacao) {
        // Agora o tipo do objeto 'avaliacao' corresponde ao tipo do LiveData
        avaliacaoData.setValue(avaliacao);
    }
    public LiveData<Avaliacao> getAvaliacao() {
        // O getter agora retorna o tipo correto
        return avaliacaoData;
    }

    // --- Métodos para as seleções da consulta ---
    public void setOpcoesConsulta(boolean avaliacao, boolean dieta) {
        isAvaliacaoSelecionada.setValue(avaliacao);
        isDietaSelecionada.setValue(dieta);
    }
    public LiveData<Boolean> getIsDietaSelecionada() {
        return isDietaSelecionada;
    }

    // Método para limpar os dados após salvar a consulta
    public void limparDados() {
        avaliacaoData.setValue(null);
        isAvaliacaoSelecionada.setValue(false);
        isDietaSelecionada.setValue(false);
    }
}
