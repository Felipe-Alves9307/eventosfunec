package br.edu.exemplo.controleeventos.service;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.exemplo.controleeventos.model.PerfilUsuario;
import br.edu.exemplo.controleeventos.model.Usuario;
import br.edu.exemplo.controleeventos.support.BancoEmMemoria;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Testes da ligação Spring Security x PostgreSQL (Parte 09).
 *
 * @author Felipe Alves
 */
class UsuarioDetailsServiceTest {

    private BancoEmMemoria banco;
    private UsuarioDetailsService service;

    @BeforeEach
    void preparar() {
        banco = new BancoEmMemoria();
        service = new UsuarioDetailsService(banco.usuarioRepository());
    }

    private List<String> papeis(UserDetails detalhes) {
        return detalhes.getAuthorities().stream()
                .map(a -> a.getAuthority()).collect(Collectors.toList());
    }

    @Test
    void participanteRecebeRolePARTICIPANTE() {
        banco.novaConta("ana@exemplo.com");

        UserDetails detalhes = service.loadUserByUsername("ana@exemplo.com");

        assertEquals("ana@exemplo.com", detalhes.getUsername());
        assertEquals("hash", detalhes.getPassword());
        assertEquals(List.of("ROLE_PARTICIPANTE"), papeis(detalhes));
    }

    @Test
    void administradorRecebeRoleADMIN() {
        Usuario admin = new Usuario();
        admin.setEmail("admin@eventos.com");
        admin.setSenha("hash");
        admin.setPerfil(PerfilUsuario.ADMIN);
        banco.usuarioRepository().save(admin);

        assertEquals(List.of("ROLE_ADMIN"), papeis(service.loadUserByUsername("admin@eventos.com")));
    }

    @Test
    void loginIgnoraMaiusculasEEspacosNoEmail() {
        banco.novaConta("ana@exemplo.com");

        assertEquals("ana@exemplo.com",
                service.loadUserByUsername("  ANA@Exemplo.COM ").getUsername());
    }

    @Test
    void usuarioInexistenteLancaUsernameNotFoundException() {
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("ninguem@exemplo.com"));
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername(null));
    }
}
