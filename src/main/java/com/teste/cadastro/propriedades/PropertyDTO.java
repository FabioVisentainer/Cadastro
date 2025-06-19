package com.teste.cadastro.propriedades;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PropertyDTO {

    private Integer id;

    @NotBlank(message = "Nome não pode estar vazio!")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    // construtor padrão
    public PropertyDTO() {
    }

    // construtor para testes e criação rápida
    public PropertyDTO(Integer propertyId, String propertyName) {
        this.id = propertyId;
        this.nome = propertyName;
    }

    public PropertyDTO(PropertyModel entity) {
        this.id = entity.getId();
        this.nome = entity.getNome();
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}