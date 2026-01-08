package com.example.nutri3.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.Locale;

public class Alimento implements Serializable {

    // --- CAMPOS ATUALIZADOS PARA CORRESPONDER AO FIREBASE ---

    @PropertyName("nome")
    private String nome;

    @PropertyName("nome_normalizado")
    private String nomeNormalizado;

    @PropertyName("kcal")
    private double energiaKcal;

    @PropertyName("carbo")
    private double carboidratosG;

    @PropertyName("proteina")
    private double proteinasG;

    @PropertyName("gordura")
    private double gordurasTotaisG;

    @PropertyName("fibra")
    private double fibrasG;

    @PropertyName("quantidade")
    private String porcaoBase;

    @PropertyName("por_unidade")
    private boolean porUnidade;

    @PropertyName("peso_medio_g")
    private double pesoMedioG;


    // --- CAMPOS CALCULADOS (NÃO VÊM DO FIREBASE) ---
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

    // ---> CAMPOS FALTANTES ADICIONADOS AQUI <---
    @Exclude
    private double quantidadeCalculada; // Guarda a quantidade digitada (seja em 'g' ou 'un')
    @Exclude
    private double pesoCalculado;       // Guarda o peso final em gramas


    // Construtor vazio obrigatório para o Firebase
    public Alimento() {}

    // Opcional: Adicionar um construtor de cópia
    public Alimento(Alimento outro) {
        // Copia todos os campos base
        this.nome = outro.nome;
        this.nomeNormalizado = outro.nomeNormalizado;
        this.energiaKcal = outro.energiaKcal;
        this.carboidratosG = outro.carboidratosG;
        this.proteinasG = outro.proteinasG;
        this.gordurasTotaisG = outro.gordurasTotaisG;
        this.fibrasG = outro.fibrasG;
        this.porcaoBase = outro.porcaoBase;
        this.porUnidade = outro.porUnidade;
        this.pesoMedioG = outro.pesoMedioG;

        // Copia todos os campos calculados
        this.energiaCalculada = outro.energiaCalculada;
        this.carboidratosCalculado = outro.carboidratosCalculado;
        this.proteinasCalculada = outro.proteinasCalculada;
        this.gordurasCalculada = outro.gordurasCalculada;
        this.descricaoPorcao = outro.descricaoPorcao;
        this.quantidadeCalculada = outro.quantidadeCalculada;
        this.pesoCalculado = outro.pesoCalculado;
    }


    // --- GETTERS E SETTERS ---

    public String getNome() { return nome; }
    public String getNomeNormalizado() { return nomeNormalizado; }
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

    // ---> GETTERS FALTANTES ADICIONADOS AQUI <---
    @Exclude
    public double getQuantidadeCalculada() { return quantidadeCalculada; }
    @Exclude
    public double getPesoCalculado() { return pesoCalculado; }


    // --- LÓGICA DE CÁLCULO ---

    public void calcularNutrientesPorPorcao(double quantidade) {
        // ---> ATUALIZAÇÃO DA LÓGICA PARA GUARDAR OS VALORES <---
        this.quantidadeCalculada = quantidade; // Guarda a quantidade que foi digitada

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
            this.pesoCalculado = quantidade * this.pesoMedioG; // Calcula e guarda o peso total
            fator = this.pesoCalculado / valorPorcaoBase;
            this.descricaoPorcao = String.format(Locale.getDefault(), "%.0f unidade(s) (%.0fg)", quantidade, this.pesoCalculado);
        } else {
            this.pesoCalculado = quantidade; // Se for por grama, o peso é a própria quantidade
            fator = quantidade / valorPorcaoBase;
            this.descricaoPorcao = String.format(Locale.getDefault(), "%.0fg", quantidade);
        }

        this.energiaCalculada = this.energiaKcal * fator;
        this.carboidratosCalculado = this.carboidratosG * fator;
        this.proteinasCalculada = this.proteinasG * fator;
        this.gordurasCalculada = this.gordurasTotaisG * fator;
    }
}
