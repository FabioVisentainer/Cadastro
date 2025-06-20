package com.teste.cadastro.propriedades;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST responsável por gerenciar as Propriedades (Propriedade).
 * Permite operações de CRUD: listar, buscar, criar, atualizar e deletar.
 */
@RestController
@RequestMapping("/propriedade")
public class PropriedadeController {

    private final PropriedadeService propriedadeService;

    public PropriedadeController(PropriedadeService propriedadeService) {
        this.propriedadeService = propriedadeService;
    }

    /**
     * Retorna a lista de todas as propriedades cadastradas.
     *
     * @return Lista de PropriedadeDTO com status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<PropriedadeDTO>> findAll() {
        return ResponseEntity.ok(propriedadeService.findAll());
    }

    /**
     * Retorna uma propriedade pelo seu ID.
     *
     * @param id Identificador da propriedade
     * @return {@link PropriedadeDTO} correspondente com status 200 (OK)
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropriedadeDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(propriedadeService.findById(id));
    }

    /**
     * Cria uma nova propriedade com os dados informados no corpo da requisição.
     *
     * @param dto           Objeto contendo os dados da nova propriedade
     * @param bindingResult Resultado da validação dos dados
     * @return {@link PropriedadeDTO} criado com status 201 (Created), ou 400 (Bad Request) se houver erros de validação
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PropriedadeDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Tratar erros, retornar bad request com mensagens
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        PropriedadeDTO created = propriedadeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Atualiza uma propriedade existente.
     *
     * @param id            Identificador da propriedade a ser atualizada
     * @param dto           Objeto contendo os novos dados da propriedade
     * @param bindingResult Resultado da validação dos dados
     * @return {@link PropriedadeDTO} atualizado com status 200 (OK), ou 400 (Bad Request) se houver erros de validação
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody PropriedadeDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        PropriedadeDTO updated = propriedadeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Exclui uma propriedade existente.
     *
     * @param id Identificador da propriedade a ser removida
     * @return Resposta com status 204 (No Content) se a exclusão for bem-sucedida
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        propriedadeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}