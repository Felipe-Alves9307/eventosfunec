package br.edu.exemplo.controleeventos.service;

import br.edu.exemplo.controleeventos.model.Participante;
import br.edu.exemplo.controleeventos.repository.ParticipanteRepository;
import br.edu.exemplo.controleeventos.util.Documentos;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regras do participante usadas por TODOS os caminhos de cadastro
 * (tela do administrador e cadastro público): CPF válido, telefone válido,
 * formatação padrão e duplicidade. Uma regra só, em um lugar só.
 *
 * @author Felipe Alves
 */
final class ValidadorParticipante {

    private ValidadorParticipante() {
    }

    /**
     * Devolve campo -> mensagem (vazio = tudo certo). Quando o CPF/telefone é
     * válido, já grava no objeto a versão formatada (é ela que vai ao banco).
     */
    static Map<String, String> validar(Participante p, ParticipanteRepository repo) {
        Map<String, String> erros = new LinkedHashMap<>();

        // ---- CPF: válido + sem duplicidade ----
        String doc = p.getDocumento();
        if (doc == null || doc.isBlank()) {
            erros.put("documento", "O CPF é obrigatório.");
        } else if (!Documentos.cpfValido(doc)) {
            erros.put("documento", "Informe um CPF válido (11 dígitos).");
        } else {
            p.setDocumento(Documentos.formatarCpf(doc));
            // Procura a versão formatada E a só-dígitos, para pegar também
            // cadastros antigos que foram salvos sem máscara.
            List<String> variantes = List.of(p.getDocumento(),
                    Documentos.somenteDigitos(p.getDocumento()));
            boolean duplicado = p.getId() == null
                    ? repo.existsByDocumentoIn(variantes)
                    : repo.existsByDocumentoInAndIdNot(variantes, p.getId());
            if (duplicado) {
                erros.put("documento", "Já existe um participante cadastrado com este CPF.");
            }
        }

        // ---- Telefone: válido + formatado ----
        String tel = p.getTelefone();
        if (tel == null || tel.isBlank()) {
            erros.put("telefone", "O telefone é obrigatório.");
        } else if (!Documentos.telefoneValido(tel)) {
            erros.put("telefone", "Informe um telefone válido com DDD, ex.: (17) 99999-0000.");
        } else {
            p.setTelefone(Documentos.formatarTelefone(tel));
        }

        // ---- E-mail duplicado (a ausência/formato já é checada pelo @Valid) ----
        String email = p.getEmail();
        if (email != null && !email.isBlank()) {
            p.setEmail(email.trim());
            // Na edição, ignoramos o próprio registro (IdNot), senão todo
            // participante "duplicaria" a si mesmo.
            boolean emailDuplicado = p.getId() == null
                    ? repo.existsByEmailIgnoreCase(p.getEmail())
                    : repo.existsByEmailIgnoreCaseAndIdNot(p.getEmail(), p.getId());
            if (emailDuplicado) {
                erros.put("email", "Já existe um participante com este e-mail.");
            }
        }
        return erros;
    }
}
