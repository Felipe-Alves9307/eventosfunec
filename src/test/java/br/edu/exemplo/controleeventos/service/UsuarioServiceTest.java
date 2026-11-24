package br.edu.exemplo.controleeventos.service;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.exemplo.controleeventos.dto.CadastroUsuarioForm;
import br.edu.exemplo.controleeventos.model.*;
import br.edu.exemplo.controleeventos.support.BancoEmMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Testes do cadastro público (Partes 10 e 11).
 *
 * @author Felipe Alves
 */
class UsuarioServiceTest {

    /** Codificador de mentira: basta para provar que a senha não é salva em texto puro. */
    private static final PasswordEncoder CODIFICADOR = new PasswordEncoder() {
        @Override
        public String encode(CharSequence senha) {
            return "{hash}" + new StringBuilder(senha).reverse();
        }

        @Override
        public boolean matches(CharSequence senha, String codificada) {
            return encode(senha).equals(codificada);
        }
    };

    private BancoEmMemoria banco;
    private UsuarioService service;

    @BeforeEach
    void preparar() {
        banco = new BancoEmMemoria();
        service = new UsuarioService(banco.usuarioRepository(),
                banco.participanteRepository(), CODIFICADOR);
    }

    private CadastroUsuarioForm form(String email) {
        CadastroUsuarioForm f = new CadastroUsuarioForm();
        f.setNome("  Ana Souza ");
        f.setEmail(email);
        f.setTelefone("(17) 99999-0000");
        f.setDocumento("529.982.247-25"); // CPF válido (usado só em exemplos)
        f.setSenha("senhaSegura1");
        f.setConfirmacaoSenha("senhaSegura1");
        return f;
    }

    @Test
    void cadastroCriaContaParticipanteVinculadaAUmParticipante() {
        Usuario usuario = service.cadastrar(form("  ANA@Exemplo.COM "));

        assertEquals(PerfilUsuario.PARTICIPANTE, usuario.getPerfil());
        assertEquals("ana@exemplo.com", usuario.getEmail());
        assertEquals("Ana Souza", usuario.getNome());
        assertNotNull(usuario.getParticipante());
        assertNotNull(usuario.getParticipante().getId());
        assertEquals("ana@exemplo.com", usuario.getParticipante().getEmail());
        assertEquals("529.982.247-25", usuario.getParticipante().getDocumento());
        assertEquals(1, banco.participantes.size());
        assertEquals(1, banco.usuarios.size());
    }

    @Test
    void senhaEhGravadaCodificadaENuncaEmTextoPuro() {
        Usuario usuario = service.cadastrar(form("ana@exemplo.com"));

        assertNotEquals("senhaSegura1", usuario.getSenha());
        assertTrue(CODIFICADOR.matches("senhaSegura1", usuario.getSenha()));
    }

    @Test
    void emailDuplicadoIgnorandoMaiusculasEhRecusado() {
        service.cadastrar(form("ana@exemplo.com"));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(form("ANA@EXEMPLO.COM")));
        assertEquals("Este e-mail já está cadastrado.", e.getMessage());
        assertEquals(1, banco.usuarios.size());
    }

    @Test
    void emailJaUsadoPorParticipanteAntigoNaoVinculaAutomaticamente() {
        Participante antigo = new Participante();
        antigo.setNome("Cadastro antigo");
        antigo.setEmail("Ana@Exemplo.com");
        antigo.setTelefone("1");
        antigo.setDocumento("2");
        banco.participanteRepository().save(antigo);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.cadastrar(form("ana@exemplo.com")));
        assertEquals("Já existe um participante com este e-mail.", e.getMessage());
        assertEquals(0, banco.usuarios.size());
        assertEquals(1, banco.participantes.size());
    }
    @Test
    void cadastroFormataCpfETelefoneAntesDeGravar() {
        CadastroUsuarioForm f = form("ana@exemplo.com");
        f.setDocumento("52998224725");
        f.setTelefone("17999990000");

        Participante p = service.cadastrar(f).getParticipante();

        assertEquals("529.982.247-25", p.getDocumento());
        assertEquals("(17) 99999-0000", p.getTelefone());
    }

    @Test
    void cpfInvalidoEhRecusadoNoCampoDocumentoENadaEhGravado() {
        CadastroUsuarioForm f = form("ana@exemplo.com");
        f.setDocumento("123.456.789-00");

        CampoInvalidoException e = assertThrows(CampoInvalidoException.class, () -> service.cadastrar(f));

        assertTrue(e.getErros().containsKey("documento"));
        assertEquals(0, banco.participantes.size());
        assertEquals(0, banco.usuarios.size());
    }

    @Test
    void cpfJaCadastradoEhRecusadoMesmoComOutroEmail() {
        service.cadastrar(form("ana@exemplo.com"));

        CampoInvalidoException e = assertThrows(CampoInvalidoException.class,
                () -> service.cadastrar(form("outra@exemplo.com")));

        assertEquals("Já existe um participante cadastrado com este CPF.", e.getErros().get("documento"));
        assertEquals(1, banco.participantes.size());
    }
}
