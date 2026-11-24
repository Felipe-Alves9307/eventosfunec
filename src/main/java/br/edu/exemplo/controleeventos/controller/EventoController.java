package br.edu.exemplo.controleeventos.controller;

import br.edu.exemplo.controleeventos.model.Evento;
import br.edu.exemplo.controleeventos.service.EventoService;
import br.edu.exemplo.controleeventos.service.InscricaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Felipe Alves
 */
@Controller
public class EventoController {

    private final EventoService eventoService;
    // ESTUDO: injetado pelo construtor (o Spring entrega a instância pronta).
    private final InscricaoService inscricaoService;

    public EventoController(EventoService eventoService,
            InscricaoService inscricaoService) {
        this.eventoService = eventoService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping("/eventos")
    public String listarEventos(Model model) {
        model.addAttribute("eventos", eventoService.listarTodos()
        );
        return "lista-eventos";
    }

    // Catálogo do participante (Tutorial 11): eventos abertos e futuros
    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        model.addAttribute(
                "eventos", eventoService.listarAbertosFuturos());
        return "catalogo-eventos";
    }

    @GetMapping("/eventos/novo")
    public String novoEvento(Model model) {
        model.addAttribute("evento", new Evento());
        return "form-evento";
    }

    @PostMapping("/eventos/salvar")
    public String salvarEvento(
            @Valid Evento evento,
            BindingResult resultado) {
        if (resultado.hasErrors()) {
            return "form-evento";
        }
        eventoService.salvar(evento);
        return "redirect:/eventos";
    }

    @GetMapping("/eventos/editar/{id}")
    public String editarEvento(@PathVariable Long id, Model model) {
        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) {
            return "redirect:/eventos";
        }
        model.addAttribute("evento", evento);
        return "form-evento";
    }

    @PostMapping("/eventos/excluir/{id}")
    public String excluirEvento(@PathVariable Long id,
            RedirectAttributes redirect) {
        // ESTUDO: a regra fica no servidor; o popup do navegador é só conforto
        // e poderia ser burlado.
        if (inscricaoService.eventoPossuiInscricoes(id)) {
            // RedirectAttributes + addFlashAttribute: a mensagem sobrevive ao
            // redirect, aparece UMA vez na próxima página e some ao recarregar.
            // O template lê como ${erro}.
            redirect.addFlashAttribute("erro",
                    "Não é possível excluir este evento: ele possui participantes inscritos.");
            return "redirect:/eventos"; // return antecipado: não exclui nada
        }
        eventoService.excluir(id); // só chega aqui se não há inscrições
        redirect.addFlashAttribute("sucesso", "Evento excluído com sucesso.");
        return "redirect:/eventos";
    }
}
