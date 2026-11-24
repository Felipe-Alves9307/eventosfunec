package br.edu.exemplo.controleeventos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Felipe Alves
 */
@Entity
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;
    
    @ManyToOne
    @JoinColumn(name = "participante_id")
    private Participante participante;
    
    private LocalDateTime dataInscricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusInscricao status = StatusInscricao.ATIVA;

    private LocalDateTime dataCancelamento;
    // getters e setters

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the evento
     */
    public Evento getEvento() {
        return evento;
    }

    /**
     * @param evento the evento to set
     */
    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    /**
     * @return the participante
     */
    public Participante getParticipante() {
        return participante;
    }

    /**
     * @param participante the participante to set
     */
    public void setParticipante(Participante participante) {
        this.participante = participante;
    }

    /**
     * @return the dataInscricao
     */
    public LocalDateTime getDataInscricao() {
        return dataInscricao;
    }

    /**
     * @param dataInscricao the dataInscricao to set
     */
    public void setDataInscricao(LocalDateTime dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public StatusInscricao getStatus() {
        return status;
    }

    public void setStatus(StatusInscricao status) {
        this.status = status;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(LocalDateTime dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    /**
     * Usada só para esconder o botão "Cancelar" na tela. A regra de verdade
     * é validada de novo no InscricaoService (esconder botão não é segurança).
     */
    @Transient
    public boolean isCancelavel() {
        if (status != StatusInscricao.ATIVA || evento == null
                || evento.getData() == null || evento.getHora() == null) {
            return false;
        }
        return LocalDateTime.of(evento.getData(), evento.getHora())
                .isAfter(LocalDateTime.now());
    }
}
