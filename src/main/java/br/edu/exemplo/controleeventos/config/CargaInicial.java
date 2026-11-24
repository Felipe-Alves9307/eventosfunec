package br.edu.exemplo.controleeventos.config;

import br.edu.exemplo.controleeventos.model.PerfilUsuario;
import br.edu.exemplo.controleeventos.model.Usuario;
import br.edu.exemplo.controleeventos.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cria o primeiro administrador no PostgreSQL (Tutorial 09), apenas se ele
 * ainda não existir. As credenciais vêm de application.properties / variáveis
 * de ambiente APP_ADMIN_EMAIL e APP_ADMIN_PASSWORD, e não ficam no código.
 *
 * @author Felipe Alves
 */
@Configuration
public class CargaInicial {

    @Bean
    CommandLineRunner criarAdministrador(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String senha) {
        return args -> {
            String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
            if (repository.findByEmail(emailNormalizado).isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail(emailNormalizado);
                admin.setSenha(passwordEncoder.encode(senha));
                admin.setPerfil(PerfilUsuario.ADMIN);
                repository.save(admin);
            }
        };
    }
}
