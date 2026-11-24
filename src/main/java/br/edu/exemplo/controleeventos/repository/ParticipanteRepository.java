package br.edu.exemplo.controleeventos.repository;

import br.edu.exemplo.controleeventos.model.Participante;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Felipe Alves
 */
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    // boolean (e não Optional): o e-mail do participante não é único no banco
    // e um Optional quebraria se houvesse dois cadastros iguais.
    boolean existsByEmailIgnoreCase(String email);

    // ESTUDO: "In" recebe uma lista (CPF formatado e só-dígitos).
    // "AndIdNot" ignora o próprio registro na edição.
    boolean existsByDocumentoIn(Collection<String> documentos);

    boolean existsByDocumentoInAndIdNot(Collection<String> documentos, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
