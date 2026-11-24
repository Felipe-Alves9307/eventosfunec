package br.edu.exemplo.controleeventos.service;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.exemplo.controleeventos.model.*;
import br.edu.exemplo.controleeventos.support.BancoEmMemoria;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes das regras das Partes 05, 11 e 12 (inscrição e cancelamento).
 *
 * @author Felipe Alves
 */
class InscricaoServiceTest {

    private BancoEmMemoria banco;
    private InscricaoService service;

    @BeforeEach
    void preparar() {
        banco = new BancoEmMemoria();
        service = new InscricaoService(
                banco.inscricaoRepository(), banco.eventoRepository(),
                banco.participanteRepository(), banco.usuarioRepository());
    }

    private Usuario novoAdmin() {
        Usuario admin = new Usuario();
        admin.setEmail("admin@eventos.com");
        admin.setPerfil(PerfilUsuario.ADMIN);
        return banco.usuarioRepository().save(admin);
    }

    @Test
    void inscreverUsuarioCriaInscricaoAtivaParaOParticipanteDaConta() {
        Usuario ana = banco.novaConta("ana@exemplo.com");
        Evento evento = banco.novoEventoFuturo(5);

        Inscricao inscricao = service.inscreverUsuario(evento.getId(), "ana@exemplo.com");

        assertEquals(StatusInscricao.ATIVA, inscricao.getStatus());
        assertEquals(ana.getParticipante().getId(), inscricao.getParticipante().getId());
        assertNotNull(inscricao.getDataInscricao());
        assertNull(inscricao.getDataCancelamento());
    }

    @Test
    void contaSemParticipanteNaoPodeSeInscrever() {
        novoAdmin();
        Evento evento = banco.novoEventoFuturo(5);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.inscreverUsuario(evento.getId(), "admin@eventos.com"));
        assertEquals("Esta conta não possui participante vinculado.", e.getMessage());
    }

    @Test
    void usuarioInexistenteOuEventoInexistenteSaoRecusados() {
        banco.novaConta("ana@exemplo.com");
        Evento evento = banco.novoEventoFuturo(5);

        assertThrows(IllegalStateException.class,
                () -> service.inscreverUsuario(evento.getId(), "ninguem@exemplo.com"));
        assertThrows(IllegalStateException.class,
                () -> service.inscreverUsuario(999L, "ana@exemplo.com"));
    }

    @Test
    void naoPermiteDuasInscricoesAtivasNoMesmoEvento() {
        banco.novaConta("ana@exemplo.com");
        Evento evento = banco.novoEventoFuturo(5);
        service.inscreverUsuario(evento.getId(), "ana@exemplo.com");

        assertThrows(IllegalStateException.class,
                () -> service.inscreverUsuario(evento.getId(), "ana@exemplo.com"));
        assertEquals(1, service.contarPorEvento(evento.getId()));
    }

    @Test
    void naoPermiteUltrapassarAsVagas() {
        banco.novaConta("ana@exemplo.com");
        banco.novaConta("beto@exemplo.com");
        Evento evento = banco.novoEventoFuturo(1);
        service.inscreverUsuario(evento.getId(), "ana@exemplo.com");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.inscreverUsuario(evento.getId(), "beto@exemplo.com"));
        assertEquals("Não há vagas disponíveis para este evento.", e.getMessage());
    }

    @Test
    void eventoNaoAbertoNaoAceitaInscricao() {
        banco.novaConta("ana@exemplo.com");
        Evento encerrado = banco.novoEvento(5, LocalDateTime.now().plusDays(3), StatusEvento.ENCERRADO);

        assertThrows(IllegalStateException.class,
                () -> service.inscreverUsuario(encerrado.getId(), "ana@exemplo.com"));
    }

    @Test
    void cancelarMantemOHistoricoELiberaAVaga() {
        banco.novaConta("ana@exemplo.com");
        banco.novaConta("beto@exemplo.com");
        Evento evento = banco.novoEventoFuturo(1);
        Inscricao deAna = service.inscreverUsuario(evento.getId(), "ana@exemplo.com");

        // vaga ocupada: Beto não entra
        assertThrows(IllegalStateException.class,
                () -> service.inscreverUsuario(evento.getId(), "beto@exemplo.com"));

        service.cancelarMinhaInscricao(deAna.getId(), "ana@exemplo.com");

        // o registro continua no banco, como CANCELADA e com data de cancelamento
        Inscricao guardada = banco.inscricoes.get(deAna.getId());
        assertNotNull(guardada);
        assertEquals(StatusInscricao.CANCELADA, guardada.getStatus());
        assertNotNull(guardada.getDataCancelamento());
        assertEquals(0, service.contarPorEvento(evento.getId()));

        // a vaga foi liberada: agora Beto consegue
        assertEquals(StatusInscricao.ATIVA,
                service.inscreverUsuario(evento.getId(), "beto@exemplo.com").getStatus());
    }

    @Test
    void depoisDeCancelarOParticipantePodeSeInscreverDeNovo() {
        banco.novaConta("ana@exemplo.com");
        Evento evento = banco.novoEventoFuturo(3);
        Inscricao primeira = service.inscreverUsuario(evento.getId(), "ana@exemplo.com");
        service.cancelarMinhaInscricao(primeira.getId(), "ana@exemplo.com");

        service.inscreverUsuario(evento.getId(), "ana@exemplo.com");

        assertEquals(2, service.listarDoUsuario("ana@exemplo.com").size());
        assertEquals(1, service.contarPorEvento(evento.getId()));
    }

    @Test
    void naoCancelaInscricaoDeOutraPessoa() {
        banco.novaConta("ana@exemplo.com");
        banco.novaConta("beto@exemplo.com");
        Evento evento = banco.novoEventoFuturo(5);
        Inscricao deBeto = service.inscreverUsuario(evento.getId(), "beto@exemplo.com");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.cancelarMinhaInscricao(deBeto.getId(), "ana@exemplo.com"));
        assertEquals("Você não pode cancelar outra inscrição.", e.getMessage());
        assertEquals(StatusInscricao.ATIVA, banco.inscricoes.get(deBeto.getId()).getStatus());
    }

    @Test
    void naoCancelaDuasVezes() {
        banco.novaConta("ana@exemplo.com");
        Evento evento = banco.novoEventoFuturo(5);
        Inscricao inscricao = service.inscreverUsuario(evento.getId(), "ana@exemplo.com");
        service.cancelarMinhaInscricao(inscricao.getId(), "ana@exemplo.com");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.cancelarMinhaInscricao(inscricao.getId(), "ana@exemplo.com"));
        assertEquals("Esta inscrição já está cancelada.", e.getMessage());
    }

    @Test
    void naoCancelaDepoisDoInicioDoEvento() {
        Usuario ana = banco.novaConta("ana@exemplo.com");
        Evento iniciado = banco.novoEvento(5, LocalDateTime.now().minusHours(2), StatusEvento.ABERTO);
        Inscricao antiga = new Inscricao();
        antiga.setEvento(iniciado);
        antiga.setParticipante(ana.getParticipante());
        antiga.setDataInscricao(LocalDateTime.now().minusDays(1));
        banco.inscricaoRepository().save(antiga);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.cancelarMinhaInscricao(antiga.getId(), "ana@exemplo.com"));
        assertEquals("Não é possível cancelar após o início do evento.", e.getMessage());
        assertFalse(antiga.isCancelavel());
    }

    @Test
    void contaSemParticipanteNaoCancelaNada() {
        novoAdmin();

        assertThrows(IllegalStateException.class,
                () -> service.cancelarMinhaInscricao(1L, "admin@eventos.com"));
    }

    @Test
    void listarDoUsuarioRetornaSomenteAsProprias_InclusiveCanceladas() {
        banco.novaConta("ana@exemplo.com");
        banco.novaConta("beto@exemplo.com");
        Evento e1 = banco.novoEventoFuturo(5);
        Evento e2 = banco.novoEventoFuturo(5);
        service.inscreverUsuario(e1.getId(), "ana@exemplo.com");
        Inscricao cancelada = service.inscreverUsuario(e2.getId(), "ana@exemplo.com");
        service.inscreverUsuario(e1.getId(), "beto@exemplo.com");
        service.cancelarMinhaInscricao(cancelada.getId(), "ana@exemplo.com");

        List<Inscricao> deAna = service.listarDoUsuario("ana@exemplo.com");

        assertEquals(2, deAna.size());
        assertTrue(deAna.stream().allMatch(i -> i.getParticipante().getEmail().equals("ana@exemplo.com")));
        assertEquals(1, service.listarDoUsuario("beto@exemplo.com").size());
    }

    @Test
    void indicadoresSeparamAtivasDoTotalHistorico() {
        banco.novaConta("ana@exemplo.com");
        banco.novaConta("beto@exemplo.com");
        Evento evento = banco.novoEventoFuturo(5);
        Inscricao deAna = service.inscreverUsuario(evento.getId(), "ana@exemplo.com");
        service.inscreverUsuario(evento.getId(), "beto@exemplo.com");
        service.cancelarMinhaInscricao(deAna.getId(), "ana@exemplo.com");

        assertEquals(1, service.contarAtivas());
        assertEquals(2, service.contarTodas());
    }

    @Test
    void administradorAindaInscreveQualquerParticipantePelaTelaAdministrativa() {
        Usuario ana = banco.novaConta("ana@exemplo.com");
        Evento evento = banco.novoEventoFuturo(2);
        Inscricao pedido = new Inscricao();
        pedido.setEvento(evento);
        pedido.setParticipante(ana.getParticipante());

        Inscricao salva = service.salvar(pedido);

        assertEquals(StatusInscricao.ATIVA, salva.getStatus());
        assertThrows(IllegalStateException.class, () -> {
            Inscricao duplicada = new Inscricao();
            duplicada.setEvento(evento);
            duplicada.setParticipante(ana.getParticipante());
            service.salvar(duplicada);
        });
    }
}
