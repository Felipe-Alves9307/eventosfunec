package br.edu.exemplo.controleeventos.config;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Informa a todas as páginas quem está logado, para que a barra de navegação
 * mostre apenas os links permitidos ao perfil (a segurança real continua no
 * SecurityConfig; esconder um link nunca substitui a autorização).
 *
 * @author Felipe Alves
 */
@ControllerAdvice
public class NavegacaoAdvice {

    @ModelAttribute
    public void dadosDaBarra(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return;
        }
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean participante = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PARTICIPANTE".equals(a.getAuthority()));
        model.addAttribute("navAdmin", admin);
        model.addAttribute("navParticipante", participante);
        model.addAttribute("navEmail", auth.getName());
    }
}
