package br.edu.exemplo.controleeventos.controller;

import br.edu.exemplo.controleeventos.service.EventoService;
import br.edu.exemplo.controleeventos.service.InscricaoService;
import br.edu.exemplo.controleeventos.service.ParticipanteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página inicial pós-login: ADMIN vê o painel; PARTICIPANTE vai ao catálogo.
 *
 * @author Felipe Alves
 */
@Controller
public class DashboardController {

    private final EventoService eventoService;
    private final ParticipanteService participanteService;
    private final InscricaoService inscricaoService;

    public DashboardController(EventoService eventoService,
                               ParticipanteService participanteService,
                               InscricaoService inscricaoService) {
        this.eventoService = eventoService;
        this.participanteService = participanteService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping("/")
    public String inicio(Authentication authentication, Model model) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!admin) {
            return "redirect:/catalogo";
        }
        model.addAttribute("totalEventos", eventoService.contar());
        model.addAttribute("totalParticipantes", participanteService.contar());
        model.addAttribute("inscricoesAtivas", inscricaoService.contarAtivas());
        model.addAttribute("totalInscricoes", inscricaoService.contarTodas());
        return "dashboard";
    }
}
