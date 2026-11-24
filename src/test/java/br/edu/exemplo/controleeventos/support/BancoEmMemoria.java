package br.edu.exemplo.controleeventos.support;

import br.edu.exemplo.controleeventos.model.*;
import br.edu.exemplo.controleeventos.repository.*;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Banco de dados falso, só em memória, para testar os Services sem Spring,
 * sem PostgreSQL e sem Mockito. Cada repositório é um proxy que implementa
 * apenas os métodos que os Services realmente chamam.
 *
 * @author Felipe Alves
 */
public class BancoEmMemoria {

    private long sequencia = 0;
    public final Map<Long, Evento> eventos = new LinkedHashMap<>();
    public final Map<Long, Participante> participantes = new LinkedHashMap<>();
    public final Map<Long, Usuario> usuarios = new LinkedHashMap<>();
    public final Map<Long, Inscricao> inscricoes = new LinkedHashMap<>();

    @FunctionalInterface
    private interface Tratador {
        Object tratar(String metodo, Object[] a) throws Throwable;
    }

    private <T> T proxy(Class<T> tipo, Tratador tratador) {
        return tipo.cast(Proxy.newProxyInstance(
                tipo.getClassLoader(), new Class<?>[]{tipo}, (p, m, a) -> {
                    if (m.getDeclaringClass() == Object.class) {
                        switch (m.getName()) {
                            case "toString": return "Fake" + tipo.getSimpleName();
                            case "hashCode": return System.identityHashCode(p);
                            default: return p == a[0];
                        }
                    }
                    return tratador.tratar(m.getName(), a == null ? new Object[0] : a);
                }));
    }

    private <E> E gravar(Map<Long, E> mapa, E entidade) {
        try {
            Long id = (Long) entidade.getClass().getMethod("getId").invoke(entidade);
            if (id == null) {
                id = ++sequencia;
                entidade.getClass().getMethod("setId", Long.class).invoke(entidade, id);
            }
            mapa.put(id, entidade);
            return entidade;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    // ---------- repositórios ----------

    public EventoRepository eventoRepository() {
        return proxy(EventoRepository.class, (m, a) -> switch (m) {
            case "save" -> gravar(eventos, (Evento) a[0]);
            case "findById" -> Optional.ofNullable(eventos.get((Long) a[0]));
            case "findAll" -> new ArrayList<>(eventos.values());
            case "count" -> (long) eventos.size();
            case "findByStatusAndDataGreaterThanEqualOrderByDataAscHoraAsc" -> eventos.values().stream()
                    .filter(e -> e.getStatus() == a[0] && !e.getData().isBefore((LocalDate) a[1]))
                    .sorted(Comparator.comparing(Evento::getData).thenComparing(Evento::getHora))
                    .collect(Collectors.toList());
            default -> throw new UnsupportedOperationException("EventoRepository." + m);
        });
    }

    public ParticipanteRepository participanteRepository() {
        return proxy(ParticipanteRepository.class, (m, a) -> switch (m) {
            case "save" -> gravar(participantes, (Participante) a[0]);
            case "findById" -> Optional.ofNullable(participantes.get((Long) a[0]));
            case "findAll" -> new ArrayList<>(participantes.values());
            case "count" -> (long) participantes.size();
            case "existsByEmailIgnoreCase" -> participantes.values().stream()
                    .anyMatch(p -> p.getEmail().equalsIgnoreCase((String) a[0]));
            case "existsByEmailIgnoreCaseAndIdNot" -> participantes.values().stream()
                    .anyMatch(p -> p.getEmail().equalsIgnoreCase((String) a[0]) && !p.getId().equals(a[1]));
            case "existsByDocumentoIn" -> participantes.values().stream()
                    .anyMatch(p -> ((Collection<?>) a[0]).contains(p.getDocumento()));
            case "existsByDocumentoInAndIdNot" -> participantes.values().stream()
                    .anyMatch(p -> ((Collection<?>) a[0]).contains(p.getDocumento()) && !p.getId().equals(a[1]));
            default -> throw new UnsupportedOperationException("ParticipanteRepository." + m);
        });
    }

    public UsuarioRepository usuarioRepository() {
        return proxy(UsuarioRepository.class, (m, a) -> switch (m) {
            case "save" -> gravar(usuarios, (Usuario) a[0]);
            case "findById" -> Optional.ofNullable(usuarios.get((Long) a[0]));
            case "count" -> (long) usuarios.size();
            case "findByEmail" -> usuarios.values().stream()
                    .filter(u -> u.getEmail().equals(a[0])).findFirst();
            case "existsByEmailIgnoreCase" -> usuarios.values().stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase((String) a[0]));
            default -> throw new UnsupportedOperationException("UsuarioRepository." + m);
        });
    }

    public InscricaoRepository inscricaoRepository() {
        return proxy(InscricaoRepository.class, (m, a) -> switch (m) {
            case "save" -> gravar(inscricoes, (Inscricao) a[0]);
            case "findById" -> Optional.ofNullable(inscricoes.get((Long) a[0]));
            case "findAll" -> new ArrayList<>(inscricoes.values());
            case "count" -> (long) inscricoes.size();
            case "deleteById" -> inscricoes.remove((Long) a[0]);
            case "countByStatus" -> inscricoes.values().stream()
                    .filter(i -> i.getStatus() == a[0]).count();
            case "countByEventoIdAndStatus" -> inscricoes.values().stream()
                    .filter(i -> i.getEvento().getId().equals(a[0]) && i.getStatus() == a[1]).count();
            case "existsByEventoIdAndParticipanteIdAndStatus" -> inscricoes.values().stream()
                    .anyMatch(i -> i.getEvento().getId().equals(a[0])
                            && i.getParticipante().getId().equals(a[1]) && i.getStatus() == a[2]);
            case "findByEventoId" -> inscricoes.values().stream()
                    .filter(i -> i.getEvento().getId().equals(a[0])).collect(Collectors.toList());
            case "findByParticipanteIdOrderByDataInscricaoDesc" -> inscricoes.values().stream()
                    .filter(i -> i.getParticipante().getId().equals(a[0]))
                    .sorted(Comparator.comparing(Inscricao::getDataInscricao).reversed())
                    .collect(Collectors.toList());
            default -> throw new UnsupportedOperationException("InscricaoRepository." + m);
        });
    }

    // ---------- atalhos para montar cenários ----------

    public Evento novoEvento(int vagas, LocalDateTime inicio, StatusEvento status) {
        Evento e = new Evento();
        e.setTitulo("Evento " + (sequencia + 1));
        e.setDescricao("Descrição");
        e.setData(inicio.toLocalDate());
        e.setHora(LocalTime.of(inicio.getHour(), inicio.getMinute()));
        e.setLocal("Auditório");
        e.setQuantidadeVagas(vagas);
        e.setStatus(status);
        return gravar(eventos, e);
    }

    public Evento novoEventoFuturo(int vagas) {
        return novoEvento(vagas, LocalDateTime.now().plusDays(10), StatusEvento.ABERTO);
    }

    /** Cria Participante + Usuario PARTICIPANTE vinculados, como o cadastro público faria. */
    public Usuario novaConta(String email) {
        Participante p = new Participante();
        p.setNome("Pessoa " + email);
        p.setEmail(email);
        p.setTelefone("(17) 90000-0000");
        p.setDocumento("000.000.000-00");
        gravar(participantes, p);

        Usuario u = new Usuario();
        u.setNome(p.getNome());
        u.setEmail(email);
        u.setSenha("hash");
        u.setPerfil(PerfilUsuario.PARTICIPANTE);
        u.setParticipante(p);
        return gravar(usuarios, u);
    }
}
