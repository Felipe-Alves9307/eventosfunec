package br.edu.exemplo.controleeventos.service;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.exemplo.controleeventos.model.Evento;
import br.edu.exemplo.controleeventos.model.StatusEvento;
import br.edu.exemplo.controleeventos.support.BancoEmMemoria;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Testes do catálogo do participante (Parte 11).
 *
 * @author Felipe Alves
 */
class EventoServiceTest {

    @Test
    void catalogoTrazSomenteEventosAbertosQueAindaNaoComecaram_EmOrdemDeData() {
        BancoEmMemoria banco = new BancoEmMemoria();
        Evento depois = banco.novoEvento(5, LocalDateTime.now().plusDays(20), StatusEvento.ABERTO);
        Evento antes = banco.novoEvento(5, LocalDateTime.now().plusDays(2), StatusEvento.ABERTO);
        banco.novoEvento(5, LocalDateTime.now().plusDays(3), StatusEvento.ENCERRADO);
        banco.novoEvento(5, LocalDateTime.now().plusDays(3), StatusEvento.CANCELADO);
        banco.novoEvento(5, LocalDateTime.now().minusHours(1), StatusEvento.ABERTO);
        banco.novoEvento(5, LocalDateTime.now().minusDays(5), StatusEvento.ABERTO);

        List<Evento> catalogo = new EventoService(banco.eventoRepository()).listarAbertosFuturos();

        assertEquals(2, catalogo.size());
        assertEquals(antes.getId(), catalogo.get(0).getId());
        assertEquals(depois.getId(), catalogo.get(1).getId());
    }
}
