package br.edu.exemplo.controleeventos.service;

import br.edu.exemplo.controleeventos.model.*;
import br.edu.exemplo.controleeventos.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Felipe Alves
 */
@Service
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;
    private final UsuarioRepository usuarioRepository;

    public InscricaoService(InscricaoRepository inscricaoRepository,EventoRepository eventoRepository,ParticipanteRepository participanteRepository,UsuarioRepository usuarioRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Inscricao salvar(Inscricao inscricao) {
        if (inscricao.getEvento() == null|| inscricao.getEvento().getId() == null) {
            throw new IllegalStateException(
                    "Selecione um evento."
            );
        }
        if (inscricao.getParticipante() == null|| inscricao.getParticipante().getId() == null) {
            throw new IllegalStateException(
                    "Selecione um participante."
            );
        }
        Evento evento = eventoRepository .findById(inscricao.getEvento().getId()).orElseThrow(()
                        -> new IllegalStateException(
                                "Evento não encontrado."
                        )
                );
        Participante participante = participanteRepository.findById(inscricao.getParticipante().getId()).orElseThrow(()
                        -> new IllegalStateException(
                                "Participante não encontrado."
                        )
                );
        if (evento.getStatus() != StatusEvento.ABERTO) {
            throw new IllegalStateException(
                    "Este evento não está aberto para inscrições."
            );
        }
        boolean jaInscrito = inscricaoRepository.existsByEventoIdAndParticipanteIdAndStatus(evento.getId(),participante.getId(),StatusInscricao.ATIVA);
        if (jaInscrito) {
            throw new IllegalStateException(
                    "Este participante já possui inscrição ativa neste evento."
            );
        }
        long totalInscritos = inscricaoRepository.countByEventoIdAndStatus(evento.getId(),StatusInscricao.ATIVA);
        if (totalInscritos >= evento.getQuantidadeVagas()) {
            throw new IllegalStateException(
                    "Não há vagas disponíveis para este evento."
            );
        }
        inscricao.setEvento(evento);
        inscricao.setParticipante(participante);
        inscricao.setStatus(StatusInscricao.ATIVA);
        inscricao.setDataCancelamento(null);
        inscricao.setDataInscricao(LocalDateTime.now());
        return inscricaoRepository.save(inscricao);
    }

    public List<Inscricao> listarTodas() {
        return inscricaoRepository.findAll();
    }

    public List<Inscricao> listarPorEvento(Long eventoId) {
        return inscricaoRepository.findByEventoId(eventoId);
    }

    /** Total de inscrições ATIVAS do evento (as canceladas liberam a vaga). */
    public long contarPorEvento(Long eventoId) {
        return inscricaoRepository.countByEventoIdAndStatus(eventoId, StatusInscricao.ATIVA);
    }

    public void excluir(Long id) {
        inscricaoRepository.deleteById(id);
    }

    // ---- Barreiras de exclusão ----
    // ESTUDO: o banco proíbe apagar um registro que outra tabela referencia
    // (chave estrangeira). Perguntamos ANTES de apagar, para avisar o usuário
    // em vez de estourar uma exceção. Ficam aqui porque este service já
    // conhece os repositórios de Inscricao e Usuario.

    /** Qualquer inscrição (ativa ou cancelada) mantém vínculo com o evento. */
    public boolean eventoPossuiInscricoes(Long eventoId) {
        return inscricaoRepository.existsByEventoId(eventoId);
    }

    /** Qualquer inscrição (ativa ou cancelada) mantém vínculo com o participante. */
    public boolean participantePossuiInscricoes(Long participanteId) {
        return inscricaoRepository.existsByParticipanteId(participanteId);
    }

    /** Participante vinculado a uma conta de login. */
    public boolean participantePossuiConta(Long participanteId) {
        return usuarioRepository.existsByParticipanteId(participanteId);
    }

    public long contarAtivas() {
        return inscricaoRepository.countByStatus(StatusInscricao.ATIVA);
    }

    /** Total histórico: ativas + canceladas. */
    public long contarTodas() {
        return inscricaoRepository.count();
    }

    // ---- Tutorial 11: inscrição autenticada e "Meus eventos" ----

    /**
     * O participante NUNCA vem do formulário: é obtido da conta autenticada.
     * Reaproveita salvar(), que valida evento aberto, vagas e duplicidade.
     */
    @Transactional
    public Inscricao inscreverUsuario(Long eventoId, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário não encontrado."));

        Participante participante = usuario.getParticipante();
        if (participante == null) {
            throw new IllegalStateException(
                    "Esta conta não possui participante vinculado.");
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalStateException(
                        "Evento não encontrado."));

        Inscricao inscricao = new Inscricao();
        inscricao.setEvento(evento);
        inscricao.setParticipante(participante);

        return salvar(inscricao); // Regras da Parte 05
    }

    @Transactional(readOnly = true)
    public List<Inscricao> listarDoUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário não encontrado."));

        if (usuario.getParticipante() == null) {
            return List.of();
        }

        return inscricaoRepository
                .findByParticipanteIdOrderByDataInscricaoDesc(
                        usuario.getParticipante().getId()
                );
    }

    // ---- Tutorial 12: cancelamento sem apagar o histórico ----

    @Transactional
    public void cancelarMinhaInscricao(Long inscricaoId, String email) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário não encontrado."));

        if (usuario.getParticipante() == null) {
            throw new IllegalStateException(
                    "Conta sem participante vinculado.");
        }

        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new IllegalStateException(
                        "Inscrição não encontrada."));

        Long dono = usuario.getParticipante().getId();
        Long inscrito = inscricao.getParticipante() == null
                ? null : inscricao.getParticipante().getId();

        if (inscrito == null || !dono.equals(inscrito)) {
            throw new IllegalStateException(
                    "Você não pode cancelar outra inscrição.");
        }

        if (inscricao.getStatus() != StatusInscricao.ATIVA) {
            throw new IllegalStateException(
                    "Esta inscrição já está cancelada.");
        }

        Evento evento = inscricao.getEvento();
        LocalDateTime inicio = LocalDateTime.of(
                evento.getData(), evento.getHora());

        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException(
                    "Não é possível cancelar após o início do evento.");
        }

        inscricao.setStatus(StatusInscricao.CANCELADA);
        inscricao.setDataCancelamento(LocalDateTime.now());
        // @Transactional grava a alteração ao concluir; sem deleteById().
    }
}
