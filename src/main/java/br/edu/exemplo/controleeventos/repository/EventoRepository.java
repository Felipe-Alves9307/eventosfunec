package br.edu.exemplo.controleeventos.repository;

import br.edu.exemplo.controleeventos.model.Evento;
import br.edu.exemplo.controleeventos.model.StatusEvento;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Felipe Alves
 */
public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByStatusAndDataGreaterThanEqualOrderByDataAscHoraAsc(
            StatusEvento status,
            LocalDate data
    );
}
