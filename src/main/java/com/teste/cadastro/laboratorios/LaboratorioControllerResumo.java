package com.teste.cadastro.laboratorios;

import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST responsável por gerenciar os Laboratórios (laboratorio).
 * Permite operações de CRUD: listar, buscar, criar, atualizar e deletar.
 */
@Validated
@RestController
@RequestMapping("/laboratorio")
public class LaboratorioControllerResumo {

    private final LaboratorioService laboratorioService;

    public LaboratorioControllerResumo(LaboratorioService laboratorioService) {
        this.laboratorioService = laboratorioService;
    }

    /**
     * Endpoint adicional: resumo de laboratórios com filtros.
     */
    @GetMapping("/resumo")
    public ResponseEntity<?> listarResumoLaboratorios(
            @RequestParam Optional<ZonedDateTime> dataInicialInicio,
            @RequestParam Optional<ZonedDateTime> dataInicialFim,
            @RequestParam Optional<ZonedDateTime> dataFinalInicio,
            @RequestParam Optional<ZonedDateTime> dataFinalFim,
            @RequestParam Optional<String> observacoes,
            @RequestParam(required = true) @Min(value = 0, message = "A quantidade mínima deve ser igual ou maior que zero.") Long quantidadeMinima
    ) {

        List<LaboratorioResumoDTO> resultado = laboratorioService.listarLaboratoriosComResumo(
                dataInicialInicio, dataInicialFim, dataFinalInicio, dataFinalFim, observacoes, quantidadeMinima
        );
        return ResponseEntity.ok(resultado);

    }
}


