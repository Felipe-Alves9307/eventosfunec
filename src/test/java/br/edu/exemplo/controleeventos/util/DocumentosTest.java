package br.edu.exemplo.controleeventos.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Testes de CPF e telefone (validação e formatação).
 *
 * @author Felipe Alves
 */
class DocumentosTest {

    @Test
    void aceitaCpfValidoComOuSemMascara() {
        assertTrue(Documentos.cpfValido("529.982.247-25"));
        assertTrue(Documentos.cpfValido("52998224725"));
    }

    @Test
    void recusaCpfComTamanhoErradoDigitoVerificadorErradoOuTodoIgual() {
        assertFalse(Documentos.cpfValido("529.982.247-2"));
        assertFalse(Documentos.cpfValido("529.982.247-26"));
        assertFalse(Documentos.cpfValido("111.111.111-11"));
        assertFalse(Documentos.cpfValido(""));
        assertFalse(Documentos.cpfValido(null));
    }

    @Test
    void formataCpfEmDigitosPuros() {
        assertEquals("529.982.247-25", Documentos.formatarCpf("52998224725"));
        assertEquals("abc", Documentos.formatarCpf("abc"));
    }

    @Test
    void validaEFormataTelefoneFixoECelular() {
        assertTrue(Documentos.telefoneValido("(17) 99999-0000"));
        assertTrue(Documentos.telefoneValido("1733334444"));
        assertFalse(Documentos.telefoneValido("17833334444")); // 11 dígitos sem o 9
        assertFalse(Documentos.telefoneValido("0733334444"));   // DDD começa com 0
        assertFalse(Documentos.telefoneValido("999990000"));    // sem DDD
        assertEquals("(17) 99999-0000", Documentos.formatarTelefone("17999990000"));
        assertEquals("(17) 3333-4444", Documentos.formatarTelefone("1733334444"));
    }
}
