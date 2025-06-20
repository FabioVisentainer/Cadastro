package com.teste.cadastro.laboratorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Repositório JPA para a entidade Laboratorio.
 * Fornece operações básicas de CRUD e métodos personalizados para validações.
 */
@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Integer> {

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

    /**
     * Lista os laboratórios com um resumo baseado em filtros específicos.
     * Esta consulta retorna uma lista de objetos do tipo LaboratorioResumoDTO, contendo o ID, nome
     * e a contagem de pessoas associadas ao laboratório. A consulta utiliza filtros para restringir
     * os resultados com base em várias condições relacionadas a datas e observações.
     *
     * Os parâmetros são:
     *
     * @param dataInicialInicio Data inicial de início do filtro de "dataInicial"
     * @param dataInicialFim Data final de fim do filtro de "dataInicial"
     * @param dataFinalInicio Data inicial de início do filtro de "dataFinal"
     * @param dataFinalFim Data final de fim do filtro de "dataFinal"
     * @param observacoes Texto a ser buscado nas observações, ignorando maiúsculas/minúsculas
     * @param quantidadeMinima Número mínimo de pessoas associadas ao laboratório para que ele seja incluído na lista
     *
     * @return Lista de objetos LaboratorioResumoDTO com o resumo dos laboratórios
     */
    @Query("SELECT new com.teste.cadastro.laboratorios.LaboratorioResumoDTO(" +
            "l.id, l.nome, COUNT(p)) " +
            "FROM Laboratorio l " +
            "JOIN Pessoa p ON p.laboratorio = l " +
            "WHERE (:dataInicialInicio IS NULL OR p.dataInicial >= :dataInicialInicio) " +
            "AND (:dataInicialFim IS NULL OR p.dataInicial <= :dataInicialFim) " +
            "AND (:dataFinalInicio IS NULL OR p.dataFinal >= :dataFinalInicio) " +
            "AND (:dataFinalFim IS NULL OR p.dataFinal <= :dataFinalFim) " +
            "AND (:observacoes IS NULL OR LOWER(p.observacoes) LIKE LOWER(CONCAT('%', :observacoes, '%'))) " +
            "GROUP BY l.id, l.nome " +
            "HAVING COUNT(p) >= :quantidadeMinima " +
            "ORDER BY COUNT(p) DESC, MIN(p.dataInicial) ASC")
    List<LaboratorioResumoDTO> listarLaboratoriosComResumo(
            @Param("dataInicialInicio") ZonedDateTime dataInicialInicio,
            @Param("dataInicialFim") ZonedDateTime dataInicialFim,
            @Param("dataFinalInicio") ZonedDateTime dataFinalInicio,
            @Param("dataFinalFim") ZonedDateTime dataFinalFim,
            @Param("observacoes") String observacoes,
            @Param("quantidadeMinima") Long quantidadeMinima
    );

}
