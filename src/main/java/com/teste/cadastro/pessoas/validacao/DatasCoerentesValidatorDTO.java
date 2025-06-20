package com.teste.cadastro.pessoas.validacao;

import com.teste.cadastro.pessoas.PessoaDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador que verifica se a dataFinal da entidade Pessoa
 * é posterior à dataInicial.
 * A validação é aplicada no nível da classe (@DatasCoerentes).
 */
public class DatasCoerentesValidatorDTO implements ConstraintValidator<DatasCoerentes, PessoaDTO> {

    /**
     * Verifica se o objeto Pessoa tem dataFinal posterior a dataInicial.
     *
     * @param dto Objeto PessoaDTO a ser validado
     * @param context Contexto da validação
     * @return true se dataFinal for posterior a dataInicial, ou se algum campo for nulo (deixando para outras anotações tratarem nulidade)
     */
    @Override
    public boolean isValid(PessoaDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getDataInicial() == null || dto.getDataFinal() == null) {
            return true;
        }
        return dto.getDataFinal().isAfter(dto.getDataInicial());
    }
}