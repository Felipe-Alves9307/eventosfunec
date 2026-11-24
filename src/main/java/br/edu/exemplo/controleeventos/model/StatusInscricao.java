package br.edu.exemplo.controleeventos.model;

/**
 * Situação de uma inscrição (Tutorial 12). Somente inscrições ATIVAS
 * ocupam vaga e impedem uma nova inscrição do mesmo participante.
 *
 * @author Felipe Alves
 */
public enum StatusInscricao {
    ATIVA,
    CANCELADA
}
