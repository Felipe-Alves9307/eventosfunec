package br.edu.exemplo.controleeventos.service;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.exemplo.controleeventos.model.Participante;
import br.edu.exemplo.controleeventos.support.BancoEmMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes das regras de CPF, telefone e duplicidade do participante.
 *
 * @author Felipe Alves
 */
class ParticipanteServiceTest {

    private BancoEmMemoria banco;
    private ParticipanteService service;

    @BeforeEach
    void preparar() {
        banco = new BancoEmMemoria();
        service = new ParticipanteService(banco.participanteRepository());
    }

    private Participante novo(String email, String cpf, String telefone) {
        Participante p = new Participante();
        p.setNome("Ana Souza");
        p.setEmail(email);
        p.setDocumento(cpf);
        p.setTelefone(telefone);
        return p;
    }

    @Test
    void salvaComCpfETelefoneFormatados() {
        Participante salvo = service.salvar(novo("ana@exemplo.com", "52998224725", "17999990000"));

        assertEquals("529.982.247-25", salvo.getDocumento());
        assertEquals("(17) 99999-0000", salvo.getTelefone());
    }

    @Test
    void recusaCpfInvalidoETelefoneInvalidoJuntos() {
        CampoInvalidoException e = assertThrows(CampoInvalidoException.class,
                () -> service.salvar(novo("ana@exemplo.com", "123", "99")));

        assertTrue(e.getErros().containsKey("documento"));
        assertTrue(e.getErros().containsKey("telefone"));
        assertEquals(0, banco.participantes.size());
    }

    @Test
    void recusaCpfDuplicadoMesmoDigitadoComOutraMascara() {
        service.salvar(novo("ana@exemplo.com", "529.982.247-25", "17999990000"));

        CampoInvalidoException e = assertThrows(CampoInvalidoException.class,
                () -> service.salvar(novo("outra@exemplo.com", "52998224725", "17988880000")));

        assertTrue(e.getErros().containsKey("documento"));
        assertEquals(1, banco.participantes.size());
    }

    @Test
    void recusaEmailDuplicadoIgnorandoMaiusculas() {
        service.salvar(novo("ana@exemplo.com", "529.982.247-25", "17999990000"));

        CampoInvalidoException e = assertThrows(CampoInvalidoException.class,
                () -> service.salvar(novo("ANA@exemplo.com", "168.995.350-09", "17988880000")));

        assertTrue(e.getErros().containsKey("email"));
    }

    @Test
    void editarOProprioParticipanteNaoContaComoDuplicidade() {
        Participante salvo = service.salvar(novo("ana@exemplo.com", "529.982.247-25", "17999990000"));

        salvo.setNome("Ana Maria Souza");
        assertDoesNotThrow(() -> service.salvar(salvo));
        assertEquals(1, banco.participantes.size());
    }
}
