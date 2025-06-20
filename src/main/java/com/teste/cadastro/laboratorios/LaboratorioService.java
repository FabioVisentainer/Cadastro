package com.teste.cadastro.laboratorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela lógica de negócio relacionada os Laboratórios.
 * Fornece métodos para operações CRUD e conversão entre modelos e DTOs.
 */
@Service
public class LaboratorioService {

    private final LaboratorioRepository laboratorioRepository;
    private final EntityManager em;

    public LaboratorioService(LaboratorioRepository laboratorioRepository, EntityManager em) {
        this.laboratorioRepository = laboratorioRepository;
        this.em = em;
    }

    /**
     * Retorna todos os laboratórios cadastrados.
     *
     * @return Lista de {@link LaboratorioDTO}
     */
    public List<LaboratorioDTO> findAll() {
        return laboratorioRepository.findAll().stream()
                .map(LaboratorioDTO::new) // Converte cada entidade para DTO
                .collect(Collectors.toList());
    }

    /**
     * Busca um laboratório pelo seu ID.
     *
     * @param id Identificador do laboratório
     * @return {@link LaboratorioDTO} correspondente
     * @throws EntityNotFoundException se o laboratório não for encontrado
     */
    public LaboratorioDTO findById(Integer id) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratório com ID " + id + " não encontrado."));
        return new LaboratorioDTO(laboratorio);
    }

    /**
     * Cria um novo laboratório.
     *
     * @param dto Dados do novo laboratório
     * @return {@link LaboratorioDTO} do laboratório criado
     * @throws IllegalArgumentException se o nome informado já estiver em uso
     */
    public LaboratorioDTO create(LaboratorioDTO dto) {
        // Validação para verificar se já existe um laboratório com o nome
        if (laboratorioRepository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Nome do laboratório já existe.");
        }
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.setNome(dto.getNome());
        laboratorio = laboratorioRepository.save(laboratorio);
        return new LaboratorioDTO(laboratorio);
    }

    /**
     * Atualiza um laboratório existente.
     *
     * @param id  Identificador do laboratório a ser atualizado
     * @param dto Dados atualizados
     * @return {@link LaboratorioDTO} com os dados atualizados
     * @throws EntityNotFoundException se o ID não existir
     * @throws IllegalArgumentException se o novo nome já estiver em uso por outro laboratório
     */
    public LaboratorioDTO update(Integer id, LaboratorioDTO dto) {
        // Validação para verificar se já existe uma propriedade com o nome
        if (laboratorioRepository.existsByNomeAndIdNot(dto.getNome(), id)) {
            throw new IllegalArgumentException("Nome do laboratório já existe.");
        }
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratório com ID " + id + " não encontrado."));

        laboratorio.setNome(dto.getNome());
        laboratorio = laboratorioRepository.save(laboratorio);
        return new LaboratorioDTO(laboratorio);
    }

    /**
     * Remove um laboratório pelo ID.
     *
     * @param id Identificador do laboratório
     * @throws EntityNotFoundException se o ID não existir
     */
    public void delete(Integer id) {
        boolean exists = laboratorioRepository.existsById(id);
        if (!exists) {
            throw new EntityNotFoundException("Laboratório com ID " + id + " não encontrado.");
        }
        laboratorioRepository.deleteById(id);
    }

    /**
     * Lista laboratórios com resumo de quantidade de pessoas, aplicando filtros e ordenações.
     */
    public List<LaboratorioResumoDTO> listarLaboratoriosComResumo(
            Optional<ZonedDateTime> dataInicialInicio,
            Optional<ZonedDateTime> dataInicialFim,
            Optional<ZonedDateTime> dataFinalInicio,
            Optional<ZonedDateTime> dataFinalFim,
            Optional<String> observacoes,
            Long quantidadeMinima
    ) {

        // Validações de datas
        if (dataInicialInicio.isPresent() && dataInicialFim.isPresent() && dataInicialInicio.get().isAfter(dataInicialFim.get())) {
            throw new IllegalArgumentException("A data inicial deve ser menor ou igual à data final.");
        }

        if (dataFinalInicio.isPresent() && dataInicialFim.isPresent() && dataFinalInicio.get().isBefore(dataInicialFim.get())) {
            throw new IllegalArgumentException("A data final inicial deve ser maior ou igual à data inicial final.");
        }

        if (dataFinalFim.isPresent() && dataFinalInicio.isPresent() && dataFinalFim.get().isBefore(dataFinalInicio.get())) {
            throw new IllegalArgumentException("A data final deve ser maior ou igual à data final inicial.");
        }

        StringBuilder jpql = new StringBuilder(
                "SELECT new com.teste.cadastro.laboratorios.LaboratorioResumoDTO(" +
                        "l.id, l.nome, COUNT(p)) " +
                        "FROM Laboratorio l " +
                        "JOIN Pessoa p ON p.laboratorio = l " +
                        "GROUP BY l.id, l.nome " +
                        "HAVING COUNT(p) >= :qMin " +
                        "ORDER BY COUNT(p) DESC"
        );

        // Filtros
        List<String> where = new ArrayList<>();
        // Faixa para Data Inicial da Pessoa (começo e fim) (opcional);
        if (dataInicialInicio.isPresent()) where.add("p.dataInicial >= :dtIniInicio");
        if (dataInicialFim.isPresent()) where.add("p.dataInicial <= :dtIniFim");
        // Faixa para Data Final da Pessoa (começo e fim) (opcional);
        if (dataFinalInicio.isPresent()) where.add("p.dataFinal >= :dtFimInicio");
        if (dataFinalFim.isPresent()) where.add("p.dataFinal <= :dtFimFim");
        //Busca de palavras em qualquer parte do campo Observações (opcional);
        if (observacoes.isPresent()) where.add("LOWER(p.observacoes) LIKE CONCAT('%', LOWER(:obs), '%')");

        if (!where.isEmpty()) {
            jpql.append(" WHERE ").append(String.join(" AND ", where));
        }

        // Caso necessário ordenar por data inicial (adiciona a ordenação) (data mais antiga por primeiro)
        if (dataInicialInicio.isPresent() || dataInicialFim.isPresent()) {
            jpql.append(", MIN(p.dataInicial) ASC");
        }

        TypedQuery<LaboratorioResumoDTO> query = em.createQuery(jpql.toString(), LaboratorioResumoDTO.class);

        // bind parâmetros de consulta:
        dataInicialInicio.ifPresent(d -> query.setParameter("dtIniInicio", d));
        dataInicialFim.ifPresent(d -> query.setParameter("dtIniFim", d));
        dataFinalInicio.ifPresent(d -> query.setParameter("dtFimInicio", d));
        dataFinalFim.ifPresent(d -> query.setParameter("dtFimFim", d));
        observacoes.ifPresent(o -> query.setParameter("obs", o));
        query.setParameter("qMin", quantidadeMinima);

        return query.getResultList();
    }

}
