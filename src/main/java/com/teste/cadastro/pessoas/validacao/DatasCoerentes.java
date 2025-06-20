package com.teste.cadastro.pessoas.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Anotação personalizada para validar se, na entidade Pessoa,
 * o atributo dataFinal é posterior ao dataInicial.
 * Deve ser aplicada na classe (nível TYPE).
 * A validação é feita pela classe {@link DatasCoerentesValidator}.
 */
@Constraint(validatedBy = { DatasCoerentesValidator.class, DatasCoerentesValidatorDTO.class })
@Target({ TYPE })
@Retention(RUNTIME)
public @interface DatasCoerentes {

    /**
     * Mensagem padrão exibida quando a validação falha.
     */
    String message() default "Data Final deve ser posterior a Data Inicial.";

    /**
     * Grupos de validação, utilizados para validações condicionais.
     */
    Class<?>[] groups() default {};

    /**
     * Payload para extensões de metadados, geralmente ignorado na maioria dos casos.
     */
    Class<? extends Payload>[] payload() default {};
}