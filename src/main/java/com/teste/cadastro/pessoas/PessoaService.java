package com.teste.cadastro.pessoas;

import com.teste.cadastro.laboratorios.Laboratorio;
import com.teste.cadastro.laboratorios.LaboratorioRepository;
import com.teste.cadastro.propriedades.Propriedade;
import com.teste.cadastro.propriedades.PropriedadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela lógica de negócio relacionada a Pessoas.
 * Fornece métodos para operações CRUD e conversão entre modelos e DTOs.
 */
@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final PropriedadeRepository propriedadeRepository;

    // Construtor recebe todas as dependências necessárias
    public PessoaService(PessoaRepository pessoaRepository,
                         LaboratorioRepository laboratorioRepository,
                         PropriedadeRepository propriedadeRepository) {
        this.pessoaRepository = pessoaRepository;
        this.laboratorioRepository = laboratorioRepository;
        this.propriedadeRepository = propriedadeRepository;
    }

    /**
     * Retorna todas as pessoas cadastradas.
     *
     * @return Lista de {@link PessoaDTO}
     */
    public List<PessoaDTO> findAll() {
        return pessoaRepository.findAll().stream()
                .map(PessoaDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma pessoa pelo seu ID.
     *
     * @param id Identificador da pessoa
     * @return {@link PessoaDTO} correspondente
     * @throws EntityNotFoundException se a pessoa não for encontrada
     */
    public PessoaDTO findById(Long id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa com ID " + id + " não encontrada."));
        return new PessoaDTO(pessoa);
    }

    /**
     * Cria uma nova pessoa.
     * Realiza validações de unicidade do nome, coerência entre as datas,
     * e existência das entidades associadas (propriedade e laboratório).
     *
     * @param dto Dados da nova pessoa a ser criada
     * @return {@link PessoaDTO} representando a pessoa criada
     * @throws IllegalArgumentException se o nome já estiver em uso ou as datas forem inválidas
     * @throws EntityNotFoundException se a propriedade ou o laboratório especificado não forem encontrados
     */
    @Transactional
    public PessoaDTO create(PessoaDTO dto) {
        //Validação para verificar se já existe uma pessoa com o nome
        if (pessoaRepository.existsByNome(dto.getNome())) {
            throw new IllegalArgumentException("Nome da pessoa já existe.");
        }
        if (!dto.getDataFinal().isAfter(dto.getDataInicial())) {
            throw new IllegalArgumentException("A data final deve ser posterior à data inicial.");
        }

        // Criando Objeto Pessoa
        Pessoa novaPessoa = new Pessoa();
        Pessoa salva = salvarPessoa(dto, novaPessoa);
        return new PessoaDTO(salva);
    }

    /**
     * Atualiza os dados de uma pessoa existente.
     * Realiza validações de unicidade do nome, coerência entre as datas,
     * e existência das entidades associadas (propriedade e laboratório).
     *
     * @param id  Identificador da pessoa a ser atualizada
     * @param dto Dados atualizados da pessoa
     * @return {@link PessoaDTO} com os dados atualizados
     * @throws EntityNotFoundException se a pessoa, a propriedade ou o laboratório não forem encontrados
     * @throws IllegalArgumentException se o novo nome já estiver em uso por outra pessoa ou as datas forem inválidas
     */
    @Transactional
    public PessoaDTO update(Long id, PessoaDTO dto) {
        // Buscando Objeto Pessoa
        Pessoa existente = pessoaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa com ID " + id + " não encontrada."));

        //Validação para verificar se já existe uma pessoa com o nome
        if (pessoaRepository.existsByNomeAndIdNot(dto.getNome(), id)) {
            throw new IllegalArgumentException("Nome da pessoa já existe.");
        }
        if (!dto.getDataFinal().isAfter(dto.getDataInicial())) {
            throw new IllegalArgumentException("A data final deve ser posterior à data inicial.");
        }

        Pessoa atualizada = salvarPessoa(dto, existente);
        return new PessoaDTO(atualizada);
    }

    /**
     * Aplica os dados do {@link PessoaDTO} a uma entidade {@link Pessoa}, realiza as associações necessárias
     * (propriedade e laboratório) e persiste a entidade no banco de dados.
     *
     * @param dto    Dados recebidos da requisição
     * @param pessoa Instância da entidade {@link Pessoa} a ser atualizada ou criada
     * @return A entidade {@link Pessoa} persistida com os dados atualizados
     * @throws EntityNotFoundException se a propriedade ou o laboratório especificado não forem encontrados
     */
    private Pessoa salvarPessoa(PessoaDTO dto, Pessoa pessoa) {
        pessoa.setNome(dto.getNome());
        pessoa.setDataInicial(dto.getDataInicial());
        pessoa.setDataFinal(dto.getDataFinal());
        pessoa.setObservacoes(dto.getObservacoes());

        // Buscar a Propriedade no banco pelo id e associar
        Propriedade propriedade = propriedadeRepository.findById(dto.getInfosPropriedade().getId())
                .orElseThrow(() -> new EntityNotFoundException("Propriedade não encontrada."));
        pessoa.setInfosPropriedade(propriedade);

        // Buscar o Laboratório no banco pelo id e associar
        Laboratorio laboratorio = laboratorioRepository.findById(dto.getLaboratorio().getId())
                .orElseThrow(() -> new EntityNotFoundException("Laboratório não encontrado."));
        pessoa.setLaboratorio(laboratorio);

        // Persistindo Objeto Pessoa
        return pessoaRepository.save(pessoa);
    }

    /**
     * Remove uma pessoa pelo ID.
     *
     * @param id Identificador da pessoa
     * @throws EntityNotFoundException se o ID não existir
     */
    public void delete(Long id) {
        boolean exists = pessoaRepository.existsById(id);
        if (!exists) {
            throw new EntityNotFoundException("Pessoa com ID " + id + " não encontrada.");
        }
        pessoaRepository.deleteById(id);
    }
}