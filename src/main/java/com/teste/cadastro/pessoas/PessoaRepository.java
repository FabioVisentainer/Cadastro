package com.teste.cadastro.pessoas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a entidade Pessoa.
 * Fornece operações básicas de CRUD e métodos personalizados para validações.
 */
@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    /**
     * Verifica se já existe alguma pessoa com o nome informado.
     * Usado para evitar nomes duplicados ao criar uma nova pessoa.
     *
     * @param nome Nome da pessoa a ser verificado
     * @return true se existir alguma pessoa com esse nome, false caso contrário
     */
    boolean existsByNome(String nome);

    /**
     * Verifica se já existe alguma pessoa com o nome informado,
     * exceto a pessoa com o ID especificado.
     * Usado para validar nomes únicos ao atualizar uma pessoa,
     * permitindo que a própria pessoa mantenha seu nome.
     *
     * @param nome Nome da pessoa a ser verificado
     * @param id ID da pessoa que está sendo atualizada (para ignorar na verificação)
     * @return true se existir outra pessoa com esse nome, false caso contrário
     */
    boolean existsByNomeAndIdNot(String nome, Long id);

}
