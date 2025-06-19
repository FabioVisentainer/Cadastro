package com.teste.cadastro.laboratorios;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela lógica de negócio relacionada os Laboratórios.
 * Fornece métodos para operações CRUD e conversão entre modelos e DTOs.
 */
@Service
public class LaboratoryService {

    private final LaboratoryRepository laboratoryRepository;

    public LaboratoryService(LaboratoryRepository laboratoryRepository) {
        this.laboratoryRepository = laboratoryRepository;
    }

    /**
     * Retorna todos os laboratórios cadastrados.
     *
     * @return Lista de {@link LaboratoryDTO}
     */
    public List<LaboratoryDTO> findAll() {
        return laboratoryRepository.findAll().stream()
                .map(LaboratoryDTO::new) // Converte cada entidade para DTO
                .collect(Collectors.toList());
    }

    /**
     * Busca um laboratório pelo seu ID.
     *
     * @param id Identificador do laboratório
     * @return {@link LaboratoryDTO} correspondente
     * @throws EntityNotFoundException se o laboratório não for encontrado
     */
    public LaboratoryDTO findById(Integer id) {
        LaboratoryModel laboratory = laboratoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratório com ID " + id + " não encontrado."));
        return new LaboratoryDTO(laboratory);
    }

    /**
     * Cria um novo laboratório.
     *
     * @param dto Dados do novo laboratório
     * @return {@link LaboratoryDTO} do laboratório criado
     * @throws IllegalArgumentException se o nome informado já estiver em uso
     */
    public LaboratoryDTO create(LaboratoryDTO dto) {
        // Validação para verificar se já existe um laboratório com o nome
        if (laboratoryRepository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Nome do laboratório já existe.");
        }
        LaboratoryModel laboratory = new LaboratoryModel();
        laboratory.setNome(dto.getNome());
        laboratory = laboratoryRepository.save(laboratory);
        return new LaboratoryDTO(laboratory);
    }

    /**
     * Atualiza um laboratório existente.
     *
     * @param id  Identificador do laboratório a ser atualizado
     * @param dto Dados atualizados
     * @return {@link LaboratoryDTO} com os dados atualizados
     * @throws EntityNotFoundException se o ID não existir
     * @throws IllegalArgumentException se o novo nome já estiver em uso por outro laboratório
     */
    public LaboratoryDTO update(Integer id, LaboratoryDTO dto) {
        // Validação para verificar se já existe uma propriedade com o nome
        if (laboratoryRepository.existsByNomeAndIdNot(dto.getNome(), id)) {
            throw new IllegalArgumentException("Nome do laboratório já existe.");
        }
        LaboratoryModel laboratory = laboratoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratório com ID " + id + " não encontrado."));

        laboratory.setNome(dto.getNome());
        laboratory = laboratoryRepository.save(laboratory);
        return new LaboratoryDTO(laboratory);
    }

    /**
     * Remove um laboratório pelo ID.
     *
     * @param id Identificador do laboratório
     * @throws EntityNotFoundException se o ID não existir
     */
    public void delete(Integer id) {
        boolean exists = laboratoryRepository.existsById(id);
        if (!exists) {
            throw new EntityNotFoundException("Laboratório com ID " + id + " não encontrado.");
        }
        laboratoryRepository.deleteById(id);
    }

}
