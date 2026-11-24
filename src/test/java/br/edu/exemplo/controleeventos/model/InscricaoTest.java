package br.edu.exemplo.controleeventos.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * @author Felipe Alves
 */
class InscricaoTest {

    private Inscricao inscricaoDoEventoEm(LocalDateTime inicio, StatusInscricao status) {
        Evento evento = new Evento();
        evento.setData(inicio.toLocalDate());
        evento.setHora(inicio.toLocalTime().withSecond(0).withNano(0));
        Inscricao inscricao = new Inscricao();
        inscricao.setEvento(evento);
        inscricao.setStatus(status);
        return inscricao;
    }

    @Test
    void novaInscricaoNasceAtivaESemDataDeCancelamento() {
        Inscricao inscricao = new Inscricao();
        assertEquals(StatusInscricao.ATIVA, inscricao.getStatus());
        assertNull(inscricao.getDataCancelamento());
    }

    @Test
    void sohPodeSerCanceladaSeAtivaEOEventoAindaNaoComecou() {
        assertTrue(inscricaoDoEventoEm(LocalDateTime.now().plusDays(1), StatusInscricao.ATIVA).isCancelavel());
        assertFalse(inscricaoDoEventoEm(LocalDateTime.now().minusMinutes(5), StatusInscricao.ATIVA).isCancelavel());
        assertFalse(inscricaoDoEventoEm(LocalDateTime.now().plusDays(1), StatusInscricao.CANCELADA).isCancelavel());
        assertFalse(new Inscricao().isCancelavel());
    }
}
