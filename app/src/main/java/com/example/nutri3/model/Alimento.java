package com.example.nutri3.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.Locale;

public class Alimento implements Serializable {

    @PropertyName("alimento")
    private String nome;

    @PropertyName("nome_busca")
    private String nomeBusca;

    @PropertyName("energia_kcal")
    private double energiaKcal;

    @PropertyName("carboidratos_g")
    private double carboidratosG;

    @PropertyName("proteinas_g")
    private double proteinasG;

    @PropertyName("gorduras_totais_g")
    private double gordurasTotaisG;

    @PropertyName("fibras_g")
    private double fibrasG;

    @PropertyName("porcao")
    private String porcaoBase;

    @PropertyName("por_unidade")
    private boolean porUnidade;

    @PropertyName("peso_medio_g")
    private double pesoMedioG;

    @Exclude
    private double energiaCalculada;
    @Exclude
    private double carboidratosCalculado;
    @Exclude
    private double proteinasCalculada;
    @Exclude
    private double gordurasCalculada;
    @Exclude
    private String descricaoPorcao;

    public Alimento() {}

    public String getNome() { return nome; }

    public void setNome(String nome) {
        this.nome = nome;
        if (nome != null) {
            this.nomeBusca = nome.toLowerCase(Locale.ROOT);
        }
    }

    public String getNomeBusca() { return nomeBusca; }

    public double getEnergiaKcal() { return energiaKcal; }
    public double getCarboidratosG() { return carboidratosG; }
    public double getProteinasG() { return proteinasG; }
    public double getGordurasTotaisG() { return gordurasTotaisG; }
    public double getFibrasG() { return fibrasG; }
    public String getPorcaoBase() { return porcaoBase; }
    public boolean isPorUnidade() { return porUnidade; }
    public double getPesoMedioG() { return pesoMedioG; }

    @Exclude
    public double getEnergiaCalculada() { return energiaCalculada; }
    @Exclude
    public double getCarboidratosCalculado() { return carboidratosCalculado; }
    @Exclude
    public double getProteinasCalculada() { return proteinasCalculada; }
    @Exclude
    public double getGordurasCalculada() { return gordurasCalculada; }
    @Exclude
    public String getDescricaoPorcao() { return descricaoPorcao; }


    public void calcularNutrientesPorPorcao(double quantidade) {
        double valorPorcaoBase = 100.0;
        if (porcaoBase != null && !porcaoBase.isEmpty()) {
            try {
                String valorNumerico = porcaoBase.replaceAll("[^0-9.]", "");
                valorPorcaoBase = Double.parseDouble(valorNumerico);
            } catch (NumberFormatException e) {
                valorPorcaoBase = 100.0;
            }
        }
        if (valorPorcaoBase == 0) valorPorcaoBase = 100.0;

        double fator;
        if (porUnidade) {
            double pesoTotal = quantidade * this.pesoMedioG;
            fator = pesoTotal / valorPorcaoBase;
            this.descricaoPorcao = String.format(Locale.getDefault(), "%.0f unidade(s) (%.0fg)", quantidade, pesoTotal);
        } else {
            fator = quantidade / valorPorcaoBase;
            this.descricaoPorcao = String.format(Locale.getDefault(), "%.0fg", quantidade);
        }

        this.energiaCalculada = this.energiaKcal * fator;
        this.carboidratosCalculado = this.carboidratosG * fator;
        this.proteinasCalculada = this.proteinasG * fator;
        this.gordurasCalculada = this.gordurasTotaisG * fator;
    }
}
