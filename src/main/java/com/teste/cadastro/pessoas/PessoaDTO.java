package com.teste.cadastro.pessoas;

import com.teste.cadastro.laboratorios.LaboratorioDTO;
import com.teste.cadastro.pessoas.validacao.DatasCoerentes;
import com.teste.cadastro.propriedades.PropriedadeDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;

@DatasCoerentes
public class PessoaDTO {

    private Long id;

    @NotBlank(message = "Nome não pode estar vazio!")
    @Size(min = 3, max = 200, message = "Nome deve ter entre 3 e 200 caracteres.")
    private String nome;

    @NotNull(message = "Data inicial é obrigatória.")
    private ZonedDateTime dataInicial;

    @NotNull(message = "Data final é obrigatória.")
    private ZonedDateTime dataFinal;

    @NotNull(message = "A propriedade é obrigatória.")
    private PropriedadeDTO infosPropriedade;

    @NotNull(message = "O laboratório é obrigatório.")
    private LaboratorioDTO laboratorio;

    // Opcional
    private String observacoes;

    public PessoaDTO() {}

    //Construtor (Necessário para o findAll)
    public PessoaDTO(Pessoa pessoa) {
        this.id = pessoa.getId();
        this.nome = pessoa.getNome();
        this.dataInicial = pessoa.getDataInicial();
        this.dataFinal = pessoa.getDataFinal();
        this.observacoes = pessoa.getObservacoes();

        if (pessoa.getInfosPropriedade() != null) {
            this.infosPropriedade = new PropriedadeDTO(pessoa.getInfosPropriedade());
        }
        if (pessoa.getLaboratorio() != null) {
            this.laboratorio = new LaboratorioDTO(pessoa.getLaboratorio());
        }
    }

    // construtor para testes e criação rápida
    public PessoaDTO(String nome, ZonedDateTime dataInicial, ZonedDateTime dataFinal, PropriedadeDTO infosPropriedade, LaboratorioDTO laboratorio, String observacoes) {
        this.nome = nome;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.infosPropriedade = infosPropriedade;
        this.laboratorio = laboratorio;
        this.observacoes = observacoes;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public ZonedDateTime getDataInicial() { return dataInicial; }
    public void setDataInicial(ZonedDateTime dataInicial) { this.dataInicial = dataInicial; }

    public ZonedDateTime getDataFinal() { return dataFinal; }
    public void setDataFinal(ZonedDateTime dataFinal) { this.dataFinal = dataFinal; }

    public PropriedadeDTO getInfosPropriedade() { return infosPropriedade; }
    public void setInfosPropriedade(PropriedadeDTO infosPropriedade) { this.infosPropriedade = infosPropriedade; }

    public LaboratorioDTO getLaboratorio() { return laboratorio; }
    public void setLaboratorio(LaboratorioDTO laboratorio) { this.laboratorio = laboratorio; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}