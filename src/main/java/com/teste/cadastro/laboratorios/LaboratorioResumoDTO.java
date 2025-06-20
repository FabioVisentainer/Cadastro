package com.teste.cadastro.laboratorios;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class LaboratorioResumoDTO {
    private int id;

    @NotNull(message = "Nome do laboratório não pode ser nulo.")
    private String nome;

    @Min(value = 0, message = "Quantidade de pessoas não pode ser negativa.")
    private Long quantidadePessoas;

    // Construtor que corresponde aos parâmetros da consulta JPQL
    public LaboratorioResumoDTO(int id, String nome, Long quantidadePessoas) {
        this.id = id;
        this.nome = nome;
        this.quantidadePessoas = quantidadePessoas;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}
    public Long getQuantidadePessoas() {return quantidadePessoas;}
    public void setQuantidadePessoas(Long quantidadePessoas) {this.quantidadePessoas = quantidadePessoas;}
}