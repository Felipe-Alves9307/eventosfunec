package br.edu.exemplo.controleeventos.util;

/**
 * Funções puras (sem Spring, sem banco) para CPF e telefone: limpar, validar
 * e formatar. Por serem estáticas e independentes, são fáceis de testar.
 *
 * @author Felipe Alves
 */
public final class Documentos {

    /**
     * true  = confere também os 2 dígitos verificadores do CPF (CPF "de verdade").
     * false = aceita qualquer sequência de 11 dígitos (útil só para testes).
     */
    private static final boolean VALIDAR_DIGITOS_VERIFICADORES = false;

    private Documentos() {
    }

    /** "(17) 99999-0000" -> "17999990000". Aceita null. */
    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    // ---------------- CPF ----------------

    public static boolean cpfValido(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() != 11) {
            return false;
        }
        if (!VALIDAR_DIGITOS_VERIFICADORES) {
            return true;
        }
        // 111.111.111-11, 000.000.000-00 etc. passam na conta, mas não existem.
        if (d.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(d, 9) == d.charAt(9) - '0'
                && digitoVerificador(d, 10) == d.charAt(10) - '0';
    }

    /**
     * Cálculo oficial: multiplica cada dígito por pesos decrescentes
     * (10..2 para o 1º dígito verificador, 11..2 para o 2º), soma,
     * e o dígito é (soma * 10) % 11, trocando 10 por 0.
     */
    private static int digitoVerificador(String d, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (d.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    /** "52998224725" -> "529.982.247-25". Se não tiver 11 dígitos, devolve o texto como veio. */
    public static String formatarCpf(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() != 11) {
            return texto;
        }
        return d.substring(0, 3) + "." + d.substring(3, 6) + "."
                + d.substring(6, 9) + "-" + d.substring(9);
    }

    // ---------------- Telefone ----------------

    /** DDD (2 dígitos, não começa com 0) + número de 8 dígitos (fixo) ou 9 dígitos começando com 9 (celular). */
    public static boolean telefoneValido(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() != 10 && d.length() != 11) {
            return false;
        }
        if (d.charAt(0) == '0') {
            return false;
        }
        return d.length() == 10 || d.charAt(2) == '9';
    }

    /** "17999990000" -> "(17) 99999-0000"; "1733334444" -> "(17) 3333-4444". */
    public static String formatarTelefone(String texto) {
        String d = somenteDigitos(texto);
        if (d.length() == 11) {
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 7) + "-" + d.substring(7);
        }
        if (d.length() == 10) {
            return "(" + d.substring(0, 2) + ") " + d.substring(2, 6) + "-" + d.substring(6);
        }
        return texto;
    }
}
