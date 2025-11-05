// No seu arquivo model/Avaliacao.java

package com.example.nutri3.model; // Verifique se o pacote está correto

public class Avaliacao {

    private double peso;
    private double altura;

    // --- NOVOS CAMPOS PARA AS 7 DOBRAS ---
    private double triceps;
    private double suprailiaca;
    private double coxa;
    private double subescapular;
    private double abdominal;
    private double peitoral; // Campo que estava faltando
    private double axilar;   // Campo que estava faltando

    private double cristaIliaca;

    // Construtor vazio é essencial para o Firebase

    public double getCristaIliaca() { // <-- NOVO GETTER
        return cristaIliaca;
    }

    public void setCristaIliaca(double cristaIliaca) { // <-- NOVO SETTER
        this.cristaIliaca = cristaIliaca;
    }

    public Avaliacao() {}

    // --- GETTERS E SETTERS PARA TODOS OS CAMPOS ---

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public double getTriceps() {
        return triceps;
    }

    public void setTriceps(double triceps) {
        this.triceps = triceps;
    }

    public double getSuprailiaca() {
        return suprailiaca;
    }

    public void setSuprailiaca(double suprailiaca) {
        this.suprailiaca = suprailiaca;
    }

    public double getCoxa() {
        return coxa;
    }

    public void setCoxa(double coxa) {
        this.coxa = coxa;
    }

    public double getSubescapular() {
        return subescapular;
    }

    public void setSubescapular(double subescapular) {
        this.subescapular = subescapular;
    }

    public double getAbdominal() {
        return abdominal;
    }

    public void setAbdominal(double abdominal) {
        this.abdominal = abdominal;
    }

    public double getPeitoral() {
        return peitoral;
    }

    public void setPeitoral(double peitoral) {
        this.peitoral = peitoral;
    }

    public double getAxilar() {
        return axilar;
    }

    public void setAxilar(double axilar) {
        this.axilar = axilar;
    }
}
