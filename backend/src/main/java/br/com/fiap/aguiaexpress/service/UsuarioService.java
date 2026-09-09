package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.AtualizarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.MostrarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarUsuarioDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.InvalidRequestException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.exception.UnauthorizedActionException;
import br.com.fiap.aguiaexpress.model.NomePerfil;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.TipoPerfilRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioRepository usuarioRepository;
    private final TipoPerfilRepository tipoPerfilRepository;
    private final PasswordEncoder passwordEncoder;

    public MostrarUsuarioDTO salvarUsuario(RegistrarUsuarioDTO usuarioDTO) {
        if (usuarioDTO.nomePerfil() != NomePerfil.OPERACIONAL) {
            throw new InvalidRequestException(
                    "O cadastro público permite apenas o perfil OPERACIONAL."
            );
        }

        return criarUsuario(usuarioDTO, NomePerfil.OPERACIONAL);
    }

    public MostrarUsuarioDTO salvarUsuarioAdmin(RegistrarUsuarioDTO usuarioDTO) {
        return criarUsuario(usuarioDTO, usuarioDTO.nomePerfil());
    }

    private MostrarUsuarioDTO criarUsuario(
            RegistrarUsuarioDTO usuarioDTO,
            NomePerfil nomePerfil
    ) {
        if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new DuplicateResourceException("Usuário já cadastrado");
        }

        TipoPerfil tipoPerfil = tipoPerfilRepository.findByNomePerfil(nomePerfil)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de perfil não encontrado."));

        Usuario usuario = new Usuario();

        usuario.setNome(usuarioDTO.nome());
        usuario.setEmail(usuarioDTO.email());
        usuario.setSenhaHash(passwordEncoder.encode(usuarioDTO.senha()));
        usuario.setCargo(usuarioDTO.cargo());
        usuario.setDepartamento(usuarioDTO.departamento());
        usuario.setTipoPerfil(tipoPerfil);

        LocalDateTime agora = LocalDateTime.now();
        usuario.setPontos(0);
        usuario.setStatus(1);
        usuario.setDataCriacao(agora);
        usuario.setDataAtualizacao(agora);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new MostrarUsuarioDTO(usuarioSalvo);
    }

    public List<MostrarUsuarioDTO> listarTodosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(MostrarUsuarioDTO::new)
                .toList();
    }

    public MostrarUsuarioDTO buscarUsuarioPorId(Long id) {
        Usuario usuario = buscarEntidadePorId(id);

        validarAcessoAoUsuario(usuario);

        return new MostrarUsuarioDTO(usuario);
    }

    public MostrarUsuarioDTO atualizarUsuario(Long id, AtualizarUsuarioDTO usuarioDTO) {
        Usuario usuario = buscarEntidadePorId(id);

        validarAcessoAoUsuario(usuario);

        if (!Objects.equals(usuario.getEmail(), usuarioDTO.email())
                && usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new DuplicateResourceException("Email já cadastrado.");
        }

        usuario.setNome(usuarioDTO.nome());
        usuario.setEmail(usuarioDTO.email());
        usuario.setCargo(usuarioDTO.cargo());
        usuario.setDepartamento(usuarioDTO.departamento());
        usuario.setDataAtualizacao(LocalDateTime.now());

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        return new MostrarUsuarioDTO(usuarioAtualizado);
    }

    public void deletarUsuarioPorId(Long id) {
        Usuario usuario = buscarEntidadePorId(id);

        usuarioRepository.delete(usuario);
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado."
                        )
                );
    }

    private void validarAcessoAoUsuario(Usuario usuarioAlvo) {
        Usuario usuarioAutenticado =
                usuarioAutenticadoService.getUsuarioAutenticado();

        boolean lider = usuarioAutenticado
                .getTipoPerfil()
                .getNomePerfil() == NomePerfil.LIDER;

        boolean proprioUsuario = Objects.equals(
                usuarioAutenticado.getId(),
                usuarioAlvo.getId()
        );

        if (!lider && !proprioUsuario) {
            throw new UnauthorizedActionException(
                    "Você só pode acessar ou atualizar seu próprio perfil."
            );
        }
    }
}
