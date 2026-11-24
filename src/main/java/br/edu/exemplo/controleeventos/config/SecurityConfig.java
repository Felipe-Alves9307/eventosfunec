package br.edu.exemplo.controleeventos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticação (Tutoriais 08 e 09) e autorização por perfil (Tutoriais 09 a 12).
 * O usuário em memória da Parte 08 foi removido: quem autentica agora é o
 * UsuarioDetailsService, consultando o PostgreSQL.
 *
 * @author Felipe Alves
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Públicas: login, cadastro, CSS e página de erro
                        .requestMatchers("/css/**", "/login", "/cadastro", "/error")
                        .permitAll()
                        // Área do participante (a ordem importa: vem antes da regra ADMIN)
                        .requestMatchers("/catalogo", "/meus-eventos",
                                "/inscricoes/minha",
                                "/minhas-inscricoes/*/cancelar")
                        .hasRole("PARTICIPANTE")
                        // Área administrativa
                        .requestMatchers("/eventos/**", "/participantes/**",
                                "/inscricoes/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        // CSRF permanece ativado: formulários usam POST + th:action.
        return http.build();
    }
}
