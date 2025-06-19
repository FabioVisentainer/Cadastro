package com.teste.cadastro.laboratorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a entidade LaboratoryModel.
 * Fornece operações básicas de CRUD e métodos personalizados para validações.
 */
@Repository
public interface LaboratoryRepository extends JpaRepository<LaboratoryModel, Integer> {

    /**
     * Verifica se já existe algum laboratório com o nome informado.
     * Usado para evitar nomes duplicados ao criar um novo laboratório.
     *
     * @param nome Nome do laboratório a ser verificado
     * @return true se existir algum laboratório com esse nome, false caso contrário
     */
    boolean existsByNome(String nome);

    /**
     * Verifica se já existe algum laboratório com o nome informado,
     * exceto o laboratório com o ID especificado.
     * Usado para validar nomes únicos ao atualizar um laboratório,
     * permitindo que o próprio laboratório mantenha seu nome.
     *
     * @param nome Nome do laboratório a ser verificado
     * @param id ID do laboratório que está sendo atualizada (para ignorar na verificação)
     * @return true se existir outro laboratório com esse nome, false caso contrário
     */
    boolean existsByNomeAndIdNot(String nome, Integer id);
}
