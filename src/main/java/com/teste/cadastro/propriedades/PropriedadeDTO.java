package com.teste.cadastro.propriedades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PropriedadeDTO {

    private Integer id;

    @NotBlank(message = "Nome não pode estar vazio!")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    // construtor padrão
    public PropriedadeDTO() {
    }

    // construtor para testes e criação rápida
    public PropriedadeDTO(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public PropriedadeDTO(Propriedade entity) {
        this.id = entity.getId();
        this.nome = entity.getNome();
    }

    // Getters e Setters
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}
}