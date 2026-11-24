package br.edu.exemplo.controleeventos.repository;

import br.edu.exemplo.controleeventos.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Felipe Alves
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    // ESTUDO: Usuario tem o campo "participante"; "ParticipanteId" navega
    // até participante.id. Serve para saber se o participante tem conta de
    // login (a coluna participante_id de Usuario também é chave estrangeira).
    boolean existsByParticipanteId(Long participanteId);
}
