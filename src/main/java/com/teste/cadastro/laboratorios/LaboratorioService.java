package com.teste.cadastro.laboratorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.tuple.Pair;

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
     * Lista os laboratórios com um resumo de quantidade de pessoas, aplicando filtros de datas e observações,
     * além de validar as condições das datas para garantir a consistência dos dados.
     *
     * Este metodo realiza validações para garantir que as datas fornecidas estejam em conformidade,
     * ou seja, que a data inicial de início seja anterior ou igual às outras datas e que as datas finais
     * não sejam anteriores às datas iniciais. Se as validações falharem, será lançada uma exceção do tipo
     * IllegalArgumentException.
     *
     * Após as validações, ele chama o repositório para buscar os laboratórios com base nos filtros informados,
     * retornando uma lista de objetos do tipo LaboratorioResumoDTO com o resumo dos laboratórios, contendo
     * informações como o ID, nome e a quantidade de pessoas associadas, além de ser possível aplicar a
     * ordenação conforme a quantidade de pessoas.
     *
     * Os parâmetros são:
     *
     * @param dataInicialInicio Data inicial de início do filtro (opcional)
     * @param dataInicialFim Data final de fim do filtro para "dataInicial" (opcional)
     * @param dataFinalInicio Data inicial de início do filtro para "dataFinal" (opcional)
     * @param dataFinalFim Data final de fim do filtro para "dataFinal" (opcional)
     * @param observacoes Texto a ser buscado nas observações (opcional)
     * @param quantidadeMinima Número mínimo de pessoas associadas ao laboratório para ser incluído na lista
     *
     * @return Lista de objetos LaboratorioResumoDTO com o resumo dos laboratórios
     *
     * @throws IllegalArgumentException caso as datas não estejam em conformidade
     */
    public List<LaboratorioResumoDTO> listarLaboratoriosComResumo(
            Optional<ZonedDateTime> dataInicialInicio,
            Optional<ZonedDateTime> dataInicialFim,
            Optional<ZonedDateTime> dataFinalInicio,
            Optional<ZonedDateTime> dataFinalFim,
            Optional<String> observacoes,
            Long quantidadeMinima
    ) {

        // Validação de dataInicialInicio
        dataInicialInicio.ifPresent(dataInicial -> {
            // Se dataInicial for posterior a qualquer uma das datas finais, lança exceção
            if (dataInicial.isAfter(dataInicialFim.orElse(null))
                    || dataInicial.isAfter(dataFinalInicio.orElse(null))
                    || dataInicial.isAfter(dataFinalFim.orElse(null)))
            {
                throw new IllegalArgumentException("A data inicial de início deve ser igual ou anterior às outras datas.");
            }
        });

        // Validação de dataInicialFim
        dataInicialFim.ifPresent(dataInicial -> {
            // Se dataInicial for posterior a qualquer uma das datas finais, lança exceção
            if (dataInicial.isBefore(dataInicialInicio.orElse(null))
                    || dataInicial.isAfter(dataFinalFim.orElse(null)))
            {
                throw new IllegalArgumentException("A data inicial de fim deve ser posterior ou igual à data inicial de início e anterior ou igual à data final de fim.");
            }
        });

        // Validação de dataFinalInicio
        dataFinalInicio.ifPresent(dataFinal -> {
            // Se dataInicial for posterior a qualquer uma das datas finais, lança exceção
            if (dataFinal.isBefore(dataInicialInicio.orElse(null))
                    || dataFinal.isAfter(dataFinalFim.orElse(null)))
            {
                throw new IllegalArgumentException("A data final inicial deve ser posterior ou igual à data inicial de início e anterior ou igual à data final de fim.");
            }
        });

        // Validação de dataFinalFim
        dataFinalFim.ifPresent(dataFinal -> {
            // Se dataInicial for posterior a qualquer uma das datas finais, lança exceção
            if (dataFinal.isBefore(dataInicialInicio.orElse(null))
                    || dataFinal.isBefore(dataInicialFim.orElse(null))
                    || dataFinal.isBefore(dataFinalInicio.orElse(null)))
            {
                throw new IllegalArgumentException("A data final final deve ser posterior ou igual às outras datas.");
            }
        });

        // Chama o repositório para realizar a consulta personalizada
        return laboratorioRepository.listarLaboratoriosComResumo(
                dataInicialInicio.orElse(null),
                dataInicialFim.orElse(null),
                dataFinalInicio.orElse(null),
                dataFinalFim.orElse(null),
                observacoes.orElse(null),
                quantidadeMinima
        );
    }
}
