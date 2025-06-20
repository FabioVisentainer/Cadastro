package com.teste.cadastro.propriedades;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a entidade Propriedade.
 * Fornece operações básicas de CRUD e métodos personalizados para validações.
 */
@Repository
public interface PropriedadeRepository extends JpaRepository<Propriedade, Integer> {

    /**
     * Verifica se já existe alguma propriedade com o nome informado.
     * Usado para evitar nomes duplicados ao criar uma nova propriedade.
     *
     * @param nome Nome da propriedade a ser verificado
     * @return true se existir alguma propriedade com esse nome, false caso contrário
     */
    boolean existsByNome(String nome);

    /**
     * Verifica se já existe alguma propriedade com o nome informado,
     * exceto a propriedade com o ID especificado.
     * Usado para validar nomes únicos ao atualizar uma propriedade,
     * permitindo que a própria propriedade mantenha seu nome.
     *
     * @param nome Nome da propriedade a ser verificado
     * @param id ID da propriedade que está sendo atualizada (para ignorar na verificação)
     * @return true se existir outra propriedade com esse nome, false caso contrário
     */
    boolean existsByNomeAndIdNot(String nome, Integer id);
}