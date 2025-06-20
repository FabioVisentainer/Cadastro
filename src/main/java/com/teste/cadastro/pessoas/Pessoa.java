package com.teste.cadastro.pessoas;

import com.teste.cadastro.laboratorios.Laboratorio;
import com.teste.cadastro.pessoas.validacao.DatasCoerentes;
import com.teste.cadastro.propriedades.Propriedade;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

@DatasCoerentes
@Entity
@Table(
        name = "tb_pessoas"
)
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome não pode estar vazio!")
    @Size(min = 3, max = 200, message = "Nome deve ter entre 3 e 200 caracteres.")
    private String nome;

    @NotNull(message = "Data inicial é obrigatória.")
    private ZonedDateTime dataInicial;

    @NotNull(message = "Data final é obrigatória.")
    private ZonedDateTime dataFinal;

    @NotNull(message = "A propriedade é obrigatória.")
    @ManyToOne
    @JoinColumn(name = "propriedade_id")
    private Propriedade infosPropriedade;

    @NotNull(message = "O laboratório é obrigatório.")
    @ManyToOne
    @JoinColumn(name = "laboratorio_id")
    private Laboratorio laboratorio;

    // Opcional
    private String observacoes;

    // Getters e Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public ZonedDateTime getDataInicial() {return dataInicial;}
    public void setDataInicial(ZonedDateTime dataInicial) {this.dataInicial = dataInicial;}

    public ZonedDateTime getDataFinal() {return dataFinal;}
    public void setDataFinal(ZonedDateTime dataFinal) {this.dataFinal = dataFinal;}

    public Propriedade getInfosPropriedade() {return infosPropriedade;}
    public void setInfosPropriedade(Propriedade infosPropriedade) {this.infosPropriedade = infosPropriedade;}

    public Laboratorio getLaboratorio() {return laboratorio;}
    public void setLaboratorio(Laboratorio laboratorio) {this.laboratorio = laboratorio;}

    public String getObservacoes() {return observacoes;}
    public void setObservacoes(String observacoes) {this.observacoes = observacoes;}
}