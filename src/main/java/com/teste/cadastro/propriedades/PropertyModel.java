package com.teste.cadastro.propriedades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "tb_property"
)
public class PropertyModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Nome não pode estar vazio!")
    @Size(min = 3, max = 100 , message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    // Getters e Setters
    public int getId() {return id;}

    public void setId(int propertyId) {this.id = propertyId;}

    public String getNome() {return nome;}

    public void setNome(String propertyName) {this.nome = propertyName;}
}