package com.example.nutri3.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nutri3.model.Alimento;
import com.example.nutri3.model.Avaliacao;
import com.example.nutri3.model.Dieta;

import java.util.Objects;

public class ConsultaViewModel extends ViewModel {

    private final MutableLiveData<String> pacienteIdSelecionado = new MutableLiveData<>();

    private final MutableLiveData<Avaliacao> dadosAvaliacao = new MutableLiveData<>();

    private final MutableLiveData<Dieta> dadosDieta = new MutableLiveData<>(new Dieta());


    public void iniciarNovaConsulta() {
        limparDados();
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

    public void adicionarAlimento(Alimento novoAlimento, String tipoRefeicao) {
        Dieta dietaAtual = dadosDieta.getValue();

        if (dietaAtual == null) {
            dietaAtual = new Dieta();
        }

        switch (tipoRefeicao) {
            case "cafeDaManha":
                dietaAtual.getCafeDaManha().add(novoAlimento);
                break;
            case "almoco":
                dietaAtual.getAlmoco().add(novoAlimento);
                break;
            case "cafeDaTarde":
                dietaAtual.getCafeDaTarde().add(novoAlimento);
                break;
            case "jantar":
                dietaAtual.getJantar().add(novoAlimento);
                break;
            case "ceia":
                dietaAtual.getCeia().add(novoAlimento);
                break;
        }

        dadosDieta.setValue(dietaAtual);
    }


    public LiveData<String> getPacienteIdSelecionado() {
        return pacienteIdSelecionado;
    }

    public LiveData<Avaliacao> getDadosAvaliacao() {
        return dadosAvaliacao;
    }

    public LiveData<Dieta> getDadosDieta() {
        return dadosDieta;
    }


    public void limparDados() {
        pacienteIdSelecionado.setValue(null);
        dadosAvaliacao.setValue(null);
        dadosDieta.setValue(new Dieta());
    }
}
