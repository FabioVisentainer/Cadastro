package com.teste.cadastro.pessoas;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST responsável por gerenciar as Pessoas (pessoa).
 * Permite operações de CRUD: listar, buscar, criar, atualizar e deletar.
 */
@RestController
@RequestMapping("/pessoa")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    /**
     * Retorna a lista de todas as pessoas cadastradas.
     *
     * @return Lista de PessoaDTO com status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<PessoaDTO>> findAll() {
        return ResponseEntity.ok(pessoaService.findAll());
    }

    /**
     * Retorna uma Pessoa pelo seu ID.
     *
     * @param id Identificador da pessoa
     * @return {@link PessoaDTO} correspondente com status 200 (OK)
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<PessoaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(pessoaService.findById(id));
    }

    /**
     * Endpoint para criar uma nova pessoa.
     * Valida os dados recebidos e, se válidos, delega a criação ao serviço.
     *
     * @param dto           Objeto contendo os dados da nova pessoa
     * @param bindingResult Resultado da validação dos dados do DTO
     * @return {@link PessoaDTO} com status 201 (Created) se bem-sucedido,
     *         ou lista de mensagens de erro com status 400 (Bad Request) se houver falhas de validação
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PessoaDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Tratar erros, retornar bad request com mensagens
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        PessoaDTO created = pessoaService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Atualiza uma pessoa existente.
     *
     * @param id            Identificador da pessoa a ser atualizado
     * @param dto           Objeto contendo os novos dados da pessoa
     * @param bindingResult Resultado da validação dos dados
     * @return {@link PessoaDTO} atualizado com status 200 (OK), ou 400 (Bad Request) se houver erros de validação
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PessoaDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        PessoaDTO updated = pessoaService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Exclui uma pessoa existente.
     *
     * @param id Identificador da pessoa ser removido
     * @return Resposta com status 204 (No Content) se a exclusão for bem-sucedida
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pessoaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}