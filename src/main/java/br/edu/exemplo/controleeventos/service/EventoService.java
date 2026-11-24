package br.edu.exemplo.controleeventos.service;

import br.edu.exemplo.controleeventos.model.Evento;
import br.edu.exemplo.controleeventos.repository.EventoRepository;
import br.edu.exemplo.controleeventos.model.StatusEvento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author Felipe Alves
 */
@Service
public class EventoService {

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    public Evento salvar(Evento evento) {
        return repository.save(evento);
    }

    public List<Evento> listarTodos() {
        return repository.findAll();
    }
    public Evento buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public long contar() {
        return repository.count();
    }

    /** Catálogo do participante: eventos ABERTOS que ainda não começaram. */
    public List<Evento> listarAbertosFuturos() {
        LocalDateTime agora = LocalDateTime.now();
        return repository
                .findByStatusAndDataGreaterThanEqualOrderByDataAscHoraAsc(
                        StatusEvento.ABERTO, LocalDate.now())
                .stream()
                .filter(e -> e.getHora() != null
                        && LocalDateTime.of(e.getData(), e.getHora()).isAfter(agora))
                .toList();
    }
}
