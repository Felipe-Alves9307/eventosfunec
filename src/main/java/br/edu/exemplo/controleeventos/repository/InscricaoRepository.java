package br.edu.exemplo.controleeventos.repository;

import br.edu.exemplo.controleeventos.model.Inscricao;
import br.edu.exemplo.controleeventos.model.StatusInscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * @author Felipe Alves
 */
public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    // Apenas inscrições do status informado (ATIVA) ocupam vaga (Tutorial 12).
    long countByEventoIdAndStatus(
            Long eventoId,
            StatusInscricao status
    );

    boolean existsByEventoIdAndParticipanteIdAndStatus(
            Long eventoId,
            Long participanteId,
            StatusInscricao status
    );

    List<Inscricao> findByEventoId(
            Long eventoId
    );

    // Inscrições da própria conta (Tutorial 11)
    List<Inscricao> findByParticipanteIdOrderByDataInscricaoDesc(
            Long participanteId
    );

    long countByStatus(StatusInscricao status);

    // ESTUDO: o Spring Data cria a consulta só pelo nome do método.
    // "existsBy" + "EventoId" => SELECT ... WHERE evento_id = ? e devolve
    // true/false. É mais leve que count(), pois para no primeiro registro.
    // Usado para barrar a exclusão de evento com inscrições.
    boolean existsByEventoId(Long eventoId);

    // Mesma ideia, olhando a coluna participante_id.
    boolean existsByParticipanteId(Long participanteId);
}
