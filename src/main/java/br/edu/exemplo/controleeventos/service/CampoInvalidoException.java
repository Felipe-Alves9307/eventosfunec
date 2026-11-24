package br.edu.exemplo.controleeventos.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Erro de validação ligado a campos do formulário (campo -> mensagem).
 * Estende IllegalArgumentException para não quebrar quem já captura essa classe.
 *
 * @author Felipe Alves
 */
public class CampoInvalidoException extends IllegalArgumentException {

    private final Map<String, String> erros;

    public CampoInvalidoException(Map<String, String> erros) {
        super(erros.values().iterator().next());
        this.erros = Collections.unmodifiableMap(new LinkedHashMap<>(erros));
    }

    public Map<String, String> getErros() {
        return erros;
    }
}
