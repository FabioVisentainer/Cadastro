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
 * Controlador REST responsável por gerenciar as Propriedades (Property).
 * Permite operações de CRUD: listar, buscar, criar, atualizar e deletar.
 */
@RestController
@RequestMapping("/property")
public class PropertyController {

    private final PropertyService service;

    public PropertyController(PropertyService service) {
        this.service = service;
    }

    /**
     * Retorna a lista de todas as propriedades cadastradas.
     *
     * @return Lista de PropertyDTO com status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<PropertyDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Retorna uma propriedade pelo seu ID.
     *
     * @param id Identificador da propriedade
     * @return {@link PropertyDTO} correspondente com status 200 (OK)
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Cria uma nova propriedade com os dados informados no corpo da requisição.
     *
     * @param dto           Objeto contendo os dados da nova propriedade
     * @param bindingResult Resultado da validação dos dados
     * @return {@link PropertyDTO} criado com status 201 (Created), ou 400 (Bad Request) se houver erros de validação
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PropertyDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Tratar erros, retornar bad request com mensagens
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        PropertyDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Atualiza uma propriedade existente.
     *
     * @param id            Identificador da propriedade a ser atualizada
     * @param dto           Objeto contendo os novos dados da propriedade
     * @param bindingResult Resultado da validação dos dados
     * @return {@link PropertyDTO} atualizado com status 200 (OK), ou 400 (Bad Request) se houver erros de validação
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody PropertyDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        PropertyDTO updated = service.update(id, dto);
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
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}