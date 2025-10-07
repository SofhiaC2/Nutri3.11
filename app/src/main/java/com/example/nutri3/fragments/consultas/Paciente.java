package com.example.nutri3.fragments.consultas;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Paciente {

    @Exclude
    private String id; // ID do paciente (chave do nó no Firebase)

    private String nome;
    private String dataNascimento;
    private String email;
    private String telefone;
    private String genero;
    private String observacoes;
    private String nutricionistaId; // Novo campo para armazenar o ID do usuário logado

    // Construtor vazio para o Firebase
    public Paciente() {
    }

    public Paciente(String nome, String dataNascimento, String email, String telefone, String genero, String observacoes, String nutricionistaId) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.telefone = telefone;
        this.genero = genero;
        this.observacoes = observacoes;
        this.nutricionistaId = nutricionistaId; // Atribui o ID do nutricionista
    }

    // --- Getters e Setters para todos os campos ---
    // (incluindo para nutricionistaId)

    @Exclude
    public String getId() { return id; }
    @Exclude
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getNutricionistaId() { return nutricionistaId; }
    public void setNutricionistaId(String nutricionistaId) { this.nutricionistaId = nutricionistaId; }
}
