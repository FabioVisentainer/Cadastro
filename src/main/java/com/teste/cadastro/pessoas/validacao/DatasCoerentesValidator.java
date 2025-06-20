package com.teste.cadastro.pessoas.validacao;

import com.teste.cadastro.pessoas.Pessoa;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador que verifica se a dataFinal da entidade Pessoa
 * é posterior à dataInicial.
 * A validação é aplicada no nível da classe (@DatasCoerentes).
 */
public class DatasCoerentesValidator implements ConstraintValidator<DatasCoerentes, Pessoa> {

    /**
     * Verifica se o objeto Pessoa tem dataFinal posterior a dataInicial.
     *
     * @param pessoa Objeto Pessoa a ser validado
     * @param context Contexto da validação
     * @return true se dataFinal for posterior a dataInicial, ou se algum campo for nulo (deixando para outras anotações tratarem nulidade)
     */
    @Override
    public boolean isValid(Pessoa pessoa, ConstraintValidatorContext context) {
        if (pessoa == null || pessoa.getDataInicial() == null || pessoa.getDataFinal() == null) {
            return true;
        }
        return pessoa.getDataFinal() != null && pessoa.getDataInicial() != null
                && pessoa.getDataFinal().isAfter(pessoa.getDataInicial());
    }

}