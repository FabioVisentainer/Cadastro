package com.teste.cadastro.laboratorios;

import com.teste.cadastro.propriedades.PropertyDTO;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST responsável por gerenciar os Laboratórios (Laboratory).
 * Permite operações de CRUD: listar, buscar, criar, atualizar e deletar.
 */
@RestController
@RequestMapping("/laboratory")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    public LaboratoryController(LaboratoryService laboratoryService) {
        this.laboratoryService = laboratoryService;
    }

    /**
     * Retorna a lista de todos os laboratórios cadastrados.
     *
     * @return Lista de LaboratoryDTO com status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<LaboratoryDTO>> findAll() {
        return ResponseEntity.ok(laboratoryService.findAll());
    }

    /**
     * Retorna um laboratório pelo seu ID.
     *
     * @param id Identificador do laboratório
     * @return {@link LaboratoryDTO} correspondente com status 200 (OK)
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @GetMapping("/{id}")
    public ResponseEntity<LaboratoryDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(laboratoryService.findById(id));
    }

    /**
     * Cria um novo laboratório com os dados informados no corpo da requisição.
     *
     * @param dto           Objeto contendo os dados do novo laboratório
     * @param bindingResult Resultado da validação dos dados
     * @return {@link LaboratoryDTO} criado com status 201 (Created), ou 400 (Bad Request) se houver erros de validação
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody LaboratoryDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Tratar erros, retornar bad request com mensagens
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        LaboratoryDTO created = laboratoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Atualiza um laboratório existente.
     *
     * @param id            Identificador do laboratório a ser atualizado
     * @param dto           Objeto contendo os novos dados do laboratório
     * @param bindingResult Resultado da validação dos dados
     * @return {@link LaboratoryDTO} atualizado com status 200 (OK), ou 400 (Bad Request) se houver erros de validação
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody LaboratoryDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        LaboratoryDTO updated = laboratoryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Exclui um laboratório existente.
     *
     * @param id Identificador do laboratório a ser removido
     * @return Resposta com status 204 (No Content) se a exclusão for bem-sucedida
     * @throws jakarta.persistence.EntityNotFoundException se o ID não existir
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        laboratoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
