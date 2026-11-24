package br.edu.exemplo.controleeventos.service;

import br.edu.exemplo.controleeventos.model.Participante;
import br.edu.exemplo.controleeventos.repository.ParticipanteRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * @author Felipe Alves
 */
@Service
public class ParticipanteService {

    private final ParticipanteRepository repository;

    public ParticipanteService(ParticipanteRepository repository) {
        this.repository = repository;
    }

    /**
     * Confere CPF, telefone e duplicidades e já formata os campos válidos.
     * Devolve campo -> mensagem; mapa vazio = pode salvar.
     */
    public Map<String, String> validar(Participante participante) {
        return ValidadorParticipante.validar(participante, repository);
    }

    /** Valida de novo (defesa no servidor) e só então grava. */
    public Participante salvar(Participante participante) {
        Map<String, String> erros = validar(participante);
        if (!erros.isEmpty()) {
            throw new CampoInvalidoException(erros);
        }
        return repository.save(participante);
    }

    public List<Participante> listarTodos() {
        return repository.findAll();
    }

    public Participante buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }

    public long contar() {
        return repository.count();
    }
}
