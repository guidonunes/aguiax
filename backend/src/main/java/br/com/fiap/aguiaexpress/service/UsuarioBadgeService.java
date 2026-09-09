package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.MostrarUsuarioBadgeDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.exception.UnauthorizedActionException;
import br.com.fiap.aguiaexpress.model.Badge;
import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.model.UsuarioBadge;
import br.com.fiap.aguiaexpress.repository.UsuarioBadgeRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuarioBadgeService {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioBadgeRepository usuarioBadgeRepository;
    private final UsuarioRepository usuarioRepository;
    private final BadgeService badgeService;

    public MostrarUsuarioBadgeDTO concederBadge(Long usuarioId, Long badgeId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        Badge badge = badgeService.buscarEntidadePorId(badgeId);

        if (usuarioBadgeRepository.existsByUsuarioAndBadge(usuario, badge)) {
            throw new DuplicateResourceException("Usuário já possui essa badge.");
        }

        UsuarioBadge usuarioBadge = new UsuarioBadge();

        usuarioBadge.setUsuario(usuario);
        usuarioBadge.setBadge(badge);
        usuarioBadge.setDataConquista(LocalDateTime.now());

        UsuarioBadge usuarioBadgeSalva = usuarioBadgeRepository.save(usuarioBadge);

        return new MostrarUsuarioBadgeDTO(usuarioBadgeSalva);
    }

    public MostrarUsuarioBadgeDTO concederBadgePorNome(Long usuarioId, String nomeBadge) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        Badge badge = badgeService.buscarEntidadePorNome(nomeBadge);

        if (usuarioBadgeRepository.existsByUsuarioAndBadge(usuario, badge)) {
            throw new DuplicateResourceException("Usuário já possui essa badge.");
        }

        UsuarioBadge usuarioBadge = new UsuarioBadge();

        usuarioBadge.setUsuario(usuario);
        usuarioBadge.setBadge(badge);
        usuarioBadge.setDataConquista(LocalDateTime.now());

        UsuarioBadge usuarioBadgeSalva = usuarioBadgeRepository.save(usuarioBadge);

        return new MostrarUsuarioBadgeDTO(usuarioBadgeSalva);
    }

    public List<MostrarUsuarioBadgeDTO> listarUsuarioBadges() {
        return usuarioBadgeRepository.findAll()
                .stream()
                .map(MostrarUsuarioBadgeDTO::new)
                .toList();
    }

    public MostrarUsuarioBadgeDTO buscarUsuarioBadgePorId(Long id) {
        UsuarioBadge usuarioBadge = buscarEntidadePorId(id);

        validarAcessoAsBadges(usuarioBadge.getUsuario());

        return new MostrarUsuarioBadgeDTO(usuarioBadge);
    }

    public List<MostrarUsuarioBadgeDTO> listarBadgesPorUsuario(Long usuarioId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);

        validarAcessoAsBadges(usuario);

        return usuarioBadgeRepository.findByUsuario(usuario)
                .stream()
                .map(MostrarUsuarioBadgeDTO::new)
                .toList();
    }

    public List<MostrarUsuarioBadgeDTO> listarUsuariosPorBadge(Long badgeId) {
        Badge badge = badgeService.buscarEntidadePorId(badgeId);

        return usuarioBadgeRepository.findByBadge(badge)
                .stream()
                .map(MostrarUsuarioBadgeDTO::new)
                .toList();
    }

    public void removerBadgeDoUsuario(Long usuarioId, Long badgeId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        Badge badge = badgeService.buscarEntidadePorId(badgeId);

        UsuarioBadge usuarioBadge = usuarioBadgeRepository.findByUsuarioAndBadge(usuario, badge)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não possui essa badge."));

        usuarioBadgeRepository.delete(usuarioBadge);
    }

    public UsuarioBadge buscarEntidadePorId(Long id) {
        return usuarioBadgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conquista de badge não encontrada."));
    }

    private Usuario buscarUsuarioPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    private void validarAcessoAsBadges(Usuario usuarioAlvo) {
        Usuario usuarioAutenticado =
                usuarioAutenticadoService.getUsuarioAutenticado();

        NomePerfil perfil = usuarioAutenticado
                .getTipoPerfil()
                .getNomePerfil();

        boolean podeConsultarTodos =
                perfil == NomePerfil.GESTOR
                        || perfil == NomePerfil.LIDER;

        boolean proprioUsuario = Objects.equals(
                usuarioAutenticado.getId(),
                usuarioAlvo.getId()
        );

        if (!podeConsultarTodos && !proprioUsuario) {
            throw new UnauthorizedActionException(
                    "Você só pode consultar suas próprias badges."
            );
        }
    }
}
