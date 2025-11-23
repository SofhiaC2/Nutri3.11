package com.example.nutri3.model;

import java.util.ArrayList;
import java.util.List;

public class Dieta {

    private List<Alimento> cafeDaManha;
    private List<Alimento> almoco;
    private List<Alimento> cafeDaTarde;
    private List<Alimento> jantar;
    private List<Alimento> ceia;

    public Dieta() {
        this.cafeDaManha = new ArrayList<>();
        this.almoco = new ArrayList<>();
        this.cafeDaTarde = new ArrayList<>();
        this.jantar = new ArrayList<>();
        this.ceia = new ArrayList<>();
    }

    public List<Alimento> getCafeDaManha() { return cafeDaManha; }
    public void setCafeDaManha(List<Alimento> cafeDaManha) { this.cafeDaManha = cafeDaManha; }

    public List<Alimento> getAlmoco() { return almoco; }
    public void setAlmoco(List<Alimento> almoco) { this.almoco = almoco; }

    public List<Alimento> getCafeDaTarde() { return cafeDaTarde; }
    public void setCafeDaTarde(List<Alimento> cafeDaTarde) { this.cafeDaTarde = cafeDaTarde; }

    public List<Alimento> getJantar() { return jantar; }
    public void setJantar(List<Alimento> jantar) { this.jantar = jantar; }

    public List<Alimento> getCeia() { return ceia; }
    public void setCeia(List<Alimento> ceia) { this.ceia = ceia; }
}
