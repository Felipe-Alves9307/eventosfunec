package br.edu.exemplo.controleeventos.controller;

import br.edu.exemplo.controleeventos.model.Evento;
import br.edu.exemplo.controleeventos.model.Inscricao;
import br.edu.exemplo.controleeventos.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Felipe Alves
 */
@Controller
public class InscricaoController {

    private final InscricaoService inscricaoService;
    private final EventoService eventoService;
    private final ParticipanteService participanteService;

    public InscricaoController(InscricaoService inscricaoService, EventoService eventoService, ParticipanteService participanteService) {
        this.inscricaoService = inscricaoService;
        this.eventoService = eventoService;
        this.participanteService = participanteService;
    }

    @GetMapping("/inscricoes")
    public String listar(Model model) {
        model.addAttribute("inscricoes", inscricaoService.listarTodas());
        return "lista-inscricoes";
    }

    @GetMapping("/inscricoes/nova")
    public String nova(Model model) {
        model.addAttribute("inscricao", new Inscricao());
        model.addAttribute("eventos", eventoService.listarTodos());
        model.addAttribute("participantes", participanteService.listarTodos());
        return "form-inscricao";
    }

    @PostMapping("/inscricoes/salvar")
    public String salvar(Inscricao inscricao, Model model) {
        try {
            inscricaoService.salvar(inscricao);
            return "redirect:/inscricoes";
        } catch (IllegalStateException e) {
            model.addAttribute("erro", e.getMessage());
            carregarListas(model);
            return "form-inscricao";
        }
    }

    private void carregarListas(Model model) {
        model.addAttribute("eventos", eventoService.listarTodos());
        model.addAttribute("participantes", participanteService.listarTodos());
    }

    @PostMapping("/inscricoes/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        inscricaoService.excluir(id);
        return "redirect:/inscricoes";
    }

    // ---- Tutorial 11: área do participante (identidade vem da sessão) ----

    @PostMapping("/inscricoes/minha")
    public String inscreverMinhaConta(
            @RequestParam Long eventoId,
            Authentication authentication,
            RedirectAttributes redirect) {
        try {
            inscricaoService.inscreverUsuario(
                    eventoId, authentication.getName());
            redirect.addFlashAttribute("sucesso", "Inscrição realizada!");
            return "redirect:/meus-eventos";
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/catalogo";
        }
    }

    @GetMapping("/meus-eventos")
    public String meusEventos(Authentication auth, Model model) {
        model.addAttribute("inscricoes",
                inscricaoService.listarDoUsuario(auth.getName()));
        return "meus-eventos";
    }

    // ---- Tutorial 12: cancelar a própria inscrição (POST, nunca GET) ----

    @PostMapping("/minhas-inscricoes/{id}/cancelar")
    public String cancelarMinhaInscricao(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirect) {
        try {
            inscricaoService.cancelarMinhaInscricao(
                    id, authentication.getName());
            redirect.addFlashAttribute(
                    "sucesso", "Inscrição cancelada com sucesso.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/meus-eventos";
    }

    @GetMapping("/eventos/{id}/inscritos")
    public String inscritosDoEvento(@PathVariable Long id,Model model) {
        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) {
            return "redirect:/eventos";
        }
        var inscricoes = inscricaoService.listarPorEvento(id);
        long total = inscricaoService.contarPorEvento(id);
        model.addAttribute("evento", evento);
        model.addAttribute("inscricoes", inscricoes);
        model.addAttribute("totalInscritos", total);
        return "inscritos-evento";
    }
}
