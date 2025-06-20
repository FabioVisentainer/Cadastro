package com.teste.cadastro.propriedades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "tb_propriedades"
)
public class Propriedade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Nome não pode estar vazio!")
    @Size(min = 3, max = 100 , message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    public Propriedade() {}
    public Propriedade(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}
}