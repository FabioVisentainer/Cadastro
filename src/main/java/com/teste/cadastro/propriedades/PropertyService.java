package com.teste.cadastro.propriedades;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela lógica de negócio relacionada às Propriedades.
 * Fornece métodos para operações CRUD e conversão entre modelos e DTOs.
 */
@Service
public class PropertyService {

    private final PropertyRepository repository;

    public PropertyService(PropertyRepository repository) {
        this.repository = repository;
    }

    /**
     * Retorna todas as propriedades cadastradas.
     *
     * @return Lista de {@link PropertyDTO}
     */
    public List<PropertyDTO> findAll() {
        return repository.findAll().stream()
                .map(PropertyDTO::new) // Converte cada entidade para DTO
                .collect(Collectors.toList());
    }

    /**
     * Busca uma propriedade pelo seu ID.
     *
     * @param id Identificador da propriedade
     * @return {@link PropertyDTO} correspondente
     * @throws EntityNotFoundException se a propriedade não for encontrada
     */
    public PropertyDTO findById(Integer id) {
        PropertyModel property = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Propriedade com ID " + id + " não encontrada."));
        return new PropertyDTO(property);
    }

    /**
     * Cria uma nova propriedade.
     *
     * @param dto Dados da nova propriedade
     * @return {@link PropertyDTO} da propriedade criada
     * @throws IllegalArgumentException se o nome informado já estiver em uso
     */
    public PropertyDTO create(PropertyDTO dto) {
        // Validação para verificar se já existe uma propriedade com o nome
        if (repository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Nome da propriedade já existe.");
        }
        PropertyModel entity = new PropertyModel();
        entity.setNome(dto.getNome());
        entity = repository.save(entity); // Persiste no banco
        return new PropertyDTO(entity);
    }

    /**
     * Atualiza uma propriedade existente.
     *
     * @param id  Identificador da propriedade a ser atualizada
     * @param dto Dados atualizados
     * @return {@link PropertyDTO} com os dados atualizados
     * @throws EntityNotFoundException se o ID não existir
     * @throws IllegalArgumentException se o novo nome já estiver em uso por outra propriedade
     */
    public PropertyDTO update(Integer id, PropertyDTO dto) {
        // Validação para verificar se já existe uma propriedade com o nome
        if (repository.existsByNomeAndIdNot(dto.getNome(), id)) {
            throw new IllegalArgumentException("Nome da propriedade já existe.");
        }
        PropertyModel entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Propriedade com ID " + id + " não encontrada."));

        entity.setNome(dto.getNome()); // Atualiza nome
        entity = repository.save(entity); // Salva alterações
        return new PropertyDTO(entity);
    }

    /**
     * Remove uma propriedade pelo ID.
     *
     * @param id Identificador da propriedade
     * @throws EntityNotFoundException se o ID não existir
     */
    public void delete(Integer id) {
        boolean exists = repository.existsById(id);
        if (!exists) {
            throw new EntityNotFoundException("Propriedade com ID " + id + " não encontrada.");
        }
        repository.deleteById(id);
    }
}