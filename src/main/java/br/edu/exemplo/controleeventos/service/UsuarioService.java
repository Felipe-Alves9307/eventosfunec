package br.edu.exemplo.controleeventos.service;

import br.edu.exemplo.controleeventos.dto.CadastroUsuarioForm;
import br.edu.exemplo.controleeventos.model.Participante;
import br.edu.exemplo.controleeventos.model.PerfilUsuario;
import br.edu.exemplo.controleeventos.model.Usuario;
import br.edu.exemplo.controleeventos.repository.ParticipanteRepository;
import br.edu.exemplo.controleeventos.repository.UsuarioRepository;
import java.util.Locale;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro público (Tutoriais 10 e 11): verifica duplicidade, aplica BCrypt,
 * fixa o perfil PARTICIPANTE e cria o Participante vinculado à conta.
 *
 * @author Felipe Alves
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ParticipanteRepository participanteRepository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          ParticipanteRepository participanteRepository,
                          PasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.participanteRepository = participanteRepository;
        this.encoder = encoder;
    }

    @Transactional
    public Usuario cadastrar(CadastroUsuarioForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado.");
        }
        // Não vincula automaticamente a um participante antigo só pelo e-mail
        // (Tutorial 11, seção 4): a titularidade precisaria ser confirmada.
        if (participanteRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Já existe um participante com este e-mail.");
        }

        Participante participante = new Participante();
        participante.setNome(form.getNome().trim());
        participante.setEmail(email);
        participante.setTelefone(form.getTelefone().trim());
        participante.setDocumento(form.getDocumento().trim());

        // Mesmas regras do cadastro feito pelo administrador (CPF, telefone,
        // duplicidade). Em caso de erro nada é gravado.
        Map<String, String> erros = ValidadorParticipante.validar(participante, participanteRepository);
        if (!erros.isEmpty()) {
            throw new CampoInvalidoException(erros);
        }
        participante = participanteRepository.save(participante);

        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(encoder.encode(form.getSenha()));
        usuario.setPerfil(PerfilUsuario.PARTICIPANTE);
        usuario.setParticipante(participante);

        return usuarioRepository.save(usuario);
    }
}
