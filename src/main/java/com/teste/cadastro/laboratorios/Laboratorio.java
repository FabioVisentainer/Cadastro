package com.teste.cadastro.laboratorios;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "tb_laboratorios"
)
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Nome não pode estar vazio!")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    public Laboratorio() {}
    public Laboratorio(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
}