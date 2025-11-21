package com.example.nutri3.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nutri3.model.Avaliacao;

public class ConsultaViewModel extends ViewModel {

    private final MutableLiveData<Avaliacao> avaliacaoData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isAvaliacaoSelecionada = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDietaSelecionada = new MutableLiveData<>(false);

    public void setAvaliacao(Avaliacao avaliacao) {
        avaliacaoData.setValue(avaliacao);
    }
    public LiveData<Avaliacao> getAvaliacao() {
        return avaliacaoData;
    }

    public void setOpcoesConsulta(boolean avaliacao, boolean dieta) {
        isAvaliacaoSelecionada.setValue(avaliacao);
        isDietaSelecionada.setValue(dieta);
    }
    public LiveData<Boolean> getIsDietaSelecionada() {
        return isDietaSelecionada;
    }

    public void limparDados() {
        avaliacaoData.setValue(null);
        isAvaliacaoSelecionada.setValue(false);
        isDietaSelecionada.setValue(false);
    }
}
