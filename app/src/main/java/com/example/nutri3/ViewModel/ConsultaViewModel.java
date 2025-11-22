package com.example.nutri3.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nutri3.model.Avaliacao;
import com.example.nutri3.model.Dieta;

public class ConsultaViewModel extends ViewModel {

    private final MutableLiveData<String> pacienteIdSelecionado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> incluiAvaliacao = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> incluiDieta = new MutableLiveData<>(false);

    private final MutableLiveData<Avaliacao> dadosAvaliacao = new MutableLiveData<>();

    private final MutableLiveData<Dieta> dadosDieta = new MutableLiveData<>();


    public void setOpcoesConsulta(boolean avaliacao, boolean dieta) {
        incluiAvaliacao.setValue(avaliacao);
        incluiDieta.setValue(dieta);
    }

    public void selecionarPaciente(String pacienteId) {
        pacienteIdSelecionado.setValue(pacienteId);
    }

    public void atualizarDadosAvaliacao(Avaliacao avaliacao) {
        dadosAvaliacao.setValue(avaliacao);
    }

    public void atualizarDadosDieta(Dieta dieta) {
        dadosDieta.setValue(dieta);
    }

    public LiveData<String> getPacienteIdSelecionado() {
        return pacienteIdSelecionado;
    }

    public LiveData<Boolean> getIncluiAvaliacao() {
        return incluiAvaliacao;
    }

    public LiveData<Boolean> getIncluiDieta() {
        return incluiDieta;
    }

    public LiveData<Avaliacao> getDadosAvaliacao() {
        return dadosAvaliacao;
    }

    public LiveData<Dieta> getDadosDieta() {
        return dadosDieta;
    }

    public void limparDados() {
        pacienteIdSelecionado.setValue(null);
        incluiAvaliacao.setValue(false);
        incluiDieta.setValue(false);
        dadosAvaliacao.setValue(null);
        dadosDieta.setValue(null);
    }
}
