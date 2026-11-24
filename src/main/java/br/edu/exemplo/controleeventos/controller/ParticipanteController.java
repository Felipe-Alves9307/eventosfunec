package br.edu.exemplo.controleeventos.controller;

import br.edu.exemplo.controleeventos.model.Participante;
import br.edu.exemplo.controleeventos.service.CampoInvalidoException;
import br.edu.exemplo.controleeventos.service.InscricaoService;
import br.edu.exemplo.controleeventos.service.ParticipanteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Felipe Alves
 */
@Controller
public class ParticipanteController {

    private final ParticipanteService participanteService;
    private final InscricaoService inscricaoService;

    public ParticipanteController(ParticipanteService participanteService,
            InscricaoService inscricaoService) {
        this.participanteService = participanteService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping("/participantes")
    public String listar(Model model) {
        model.addAttribute("participantes", participanteService.listarTodos());
        return "lista-participantes";

    }

    @GetMapping("/participantes/novo")
    public String novo(Model model) {
        model.addAttribute("participante",new Participante());
        return "form-participante";
    }

    @PostMapping("/participantes/salvar")
    public String salvar(@Valid Participante participante,BindingResult resultado) {
        // ESTUDO: além do @Valid, aplica CPF válido, telefone válido e duplicidade.
        // Só acrescenta o erro se o campo ainda não tiver um do @Valid (evita
        // duas mensagens no mesmo campo). Também já formata CPF e telefone.
        participanteService.validar(participante).forEach((campo, mensagem) -> {
            if (!resultado.hasFieldErrors(campo)) {
                resultado.rejectValue(campo, "", mensagem);
            }
        });
        if (resultado.hasErrors()) {
            return "form-participante"; // volta ao formulário mostrando os erros
        }
        try {
            participanteService.salvar(participante);
        } catch (CampoInvalidoException e) {
            // Rede de segurança: dois cadastros iguais enviados ao mesmo tempo.
            e.getErros().forEach((campo, mensagem) ->
                    resultado.rejectValue(campo, "", mensagem));
            return "form-participante";
        }
        return "redirect:/participantes";
    }

    @GetMapping("/participantes/editar/{id}")
    public String editar(@PathVariable Long id,Model model) {
        Participante participante = participanteService.buscarPorId(id);
        if (participante == null) {
            return "redirect:/participantes";
        }
        model.addAttribute("participante",participante);
        return "form-participante";
    }

    @PostMapping("/participantes/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        // ESTUDO: duas barreiras, uma por tabela que referencia o participante.
        // Flash attribute "erro" => banner vermelho; "sucesso" => banner verde.
        // 1) inscrito em algum evento?
        if (inscricaoService.participantePossuiInscricoes(id)) {
            redirect.addFlashAttribute("erro",
                    "Não é possível excluir este participante: ele está inscrito em um ou mais eventos.");
            return "redirect:/participantes";
        }
        // 2) tem conta de login vinculada?
        if (inscricaoService.participantePossuiConta(id)) {
            redirect.addFlashAttribute("erro",
                    "Não é possível excluir este participante: ele possui uma conta de acesso vinculada.");
            return "redirect:/participantes";
        }
        participanteService.excluir(id);
        redirect.addFlashAttribute("sucesso", "Participante excluído com sucesso.");
        return "redirect:/participantes";
    }
}
