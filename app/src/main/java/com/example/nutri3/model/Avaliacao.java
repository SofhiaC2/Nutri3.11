package com.example.nutri3.model;

public class Avaliacao {

    // Adicione aqui todos os campos do seu formulário
    private double peso;
    private double altura;
    private double cintura;
    private double quadril;
    private double braco;
    private double coxa;
    private double panturrilha;
    private double triceps;
    private double subescapular;
    private double suprailiaca;
    private double abdominal;
    private double coxaDobras;

    // Construtor vazio (necessário para Firebase)
    public Avaliacao() {}

    // Getters e Setters para todos os campos
    // (Você pode gerar isso automaticamente no Android Studio:
    // Clique com o botão direito -> Generate -> Getter and Setter -> Selecione todos os campos)

    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }

    public double getAltura() { return altura; }
    public void setAltura(double altura) { this.altura = altura; }

    public double getCintura() { return cintura; }
    public void setCintura(double cintura) { this.cintura = cintura; }

    public double getQuadril() { return quadril; }
    public void setQuadril(double quadril) { this.quadril = quadril; }

    public double getBraco() { return braco; }
    public void setBraco(double braco) { this.braco = braco; }

    public double getCoxa() { return coxa; }
    public void setCoxa(double coxa) { this.coxa = coxa; }

    public double getPanturrilha() { return panturrilha; }
    public void setPanturrilha(double panturrilha) { this.panturrilha = panturrilha; }

    public double getTriceps() { return triceps; }
    public void setTriceps(double triceps) { this.triceps = triceps; }

    public double getSubescapular() { return subescapular; }
    public void setSubescapular(double subescapular) { this.subescapular = subescapular; }

    public double getSuprailiaca() { return suprailiaca; }
    public void setSuprailiaca(double suprailiaca) { this.suprailiaca = suprailiaca; }

    public double getAbdominal() { return abdominal; }
    public void setAbdominal(double abdominal) { this.abdominal = abdominal; }

    public double getCoxaDobras() { return coxaDobras; }
    public void setCoxaDobras(double coxaDobras) { this.coxaDobras = coxaDobras; }
}

