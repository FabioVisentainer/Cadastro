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
public class PropriedadeService {

    private final PropriedadeRepository propriedadeRepository;

    public PropriedadeService(PropriedadeRepository propriedadeRepository) {
        this.propriedadeRepository = propriedadeRepository;
    }

    /**
     * Retorna todas as propriedades cadastradas.
     *
     * @return Lista de {@link PropriedadeDTO}
     */
    public List<PropriedadeDTO> findAll() {
        return propriedadeRepository.findAll().stream()
                .map(PropriedadeDTO::new) // Converte cada entidade para DTO
                .collect(Collectors.toList());
    }

    /**
     * Busca uma propriedade pelo seu ID.
     *
     * @param id Identificador da propriedade
     * @return {@link PropriedadeDTO} correspondente
     * @throws EntityNotFoundException se a propriedade não for encontrada
     */
    public PropriedadeDTO findById(Integer id) {
        Propriedade propriedade = propriedadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Propriedade com ID " + id + " não encontrada."));
        return new PropriedadeDTO(propriedade);
    }

    /**
     * Cria uma nova propriedade.
     *
     * @param dto Dados da nova propriedade
     * @return {@link PropriedadeDTO} da propriedade criada
     * @throws IllegalArgumentException se o nome informado já estiver em uso
     */
    public PropriedadeDTO create(PropriedadeDTO dto) {
        // Validação para verificar se já existe uma propriedade com o nome
        if (propriedadeRepository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Nome da propriedade já existe.");
        }
        Propriedade propriedade = new Propriedade();
        propriedade.setNome(dto.getNome());
        propriedade = propriedadeRepository.save(propriedade); // Persiste no banco
        return new PropriedadeDTO(propriedade);
    }

    /**
     * Atualiza uma propriedade existente.
     *
     * @param id  Identificador da propriedade a ser atualizada
     * @param dto Dados atualizados
     * @return {@link PropriedadeDTO} com os dados atualizados
     * @throws EntityNotFoundException se o ID não existir
     * @throws IllegalArgumentException se o novo nome já estiver em uso por outra propriedade
     */
    public PropriedadeDTO update(Integer id, PropriedadeDTO dto) {
        // Validação para verificar se já existe uma propriedade com o nome
        if (propriedadeRepository.existsByNomeAndIdNot(dto.getNome(), id)) {
            throw new IllegalArgumentException("Nome da propriedade já existe.");
        }
        Propriedade propriedade = propriedadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Propriedade com ID " + id + " não encontrada."));

        propriedade.setNome(dto.getNome()); // Atualiza nome
        propriedade = propriedadeRepository.save(propriedade); // Salva alterações
        return new PropriedadeDTO(propriedade);
    }

    /**
     * Remove uma propriedade pelo ID.
     *
     * @param id Identificador da propriedade
     * @throws EntityNotFoundException se o ID não existir
     */
    public void delete(Integer id) {
        boolean exists = propriedadeRepository.existsById(id);
        if (!exists) {
            throw new EntityNotFoundException("Propriedade com ID " + id + " não encontrada.");
        }
        propriedadeRepository.deleteById(id);
    }
}