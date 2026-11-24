package br.edu.exemplo.controleeventos.controller;

import br.edu.exemplo.controleeventos.dto.CadastroUsuarioForm;
import br.edu.exemplo.controleeventos.service.CampoInvalidoException;
import br.edu.exemplo.controleeventos.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Cadastro público de contas PARTICIPANTE (Tutoriais 10 e 11).
 *
 * @author Felipe Alves
 */
@Controller
public class CadastroController {

    private final UsuarioService usuarioService;

    public CadastroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/cadastro")
    public String novo(Model model) {
        model.addAttribute("cadastro", new CadastroUsuarioForm());
        return "cadastro-usuario";
    }

    @PostMapping("/cadastro")
    public String salvar(
            @Valid @ModelAttribute("cadastro") CadastroUsuarioForm form,
            BindingResult resultado) {

        if (form.getSenha() != null && !form.getSenha().equals(form.getConfirmacaoSenha())) {
            resultado.rejectValue("confirmacaoSenha", "",
                    "As senhas informadas não coincidem.");
        }
        if (resultado.hasErrors()) {
            return "cadastro-usuario";
        }
        try {
            usuarioService.cadastrar(form);
        } catch (CampoInvalidoException e) {
            // ESTUDO: cada mensagem vai para o campo certo (CPF, telefone ou e-mail).
            e.getErros().forEach((campo, mensagem) ->
                    resultado.rejectValue(campo, "", mensagem));
            return "cadastro-usuario";
        } catch (IllegalArgumentException e) {
            resultado.rejectValue("email", "", e.getMessage());
            return "cadastro-usuario";
        } catch (DataIntegrityViolationException e) {
            // Duas requisições simultâneas com o mesmo e-mail: a restrição UNIQUE do banco barra a segunda.
            resultado.rejectValue("email", "", "Este e-mail já está cadastrado.");
            return "cadastro-usuario";
        }
        return "redirect:/login?cadastro";
    }
}
