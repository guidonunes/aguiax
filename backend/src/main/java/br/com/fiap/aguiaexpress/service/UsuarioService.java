package br.com.fiap.aguiaexpress.service;


import br.com.fiap.aguiaexpress.dto.AtualizarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.MostrarUsuarioDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarUsuarioDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.TipoPerfil;
import br.com.fiap.aguiaexpress.model.Usuario;
import br.com.fiap.aguiaexpress.repository.TipoPerfilRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TipoPerfilRepository tipoPerfilRepository;

    public MostrarUsuarioDTO salvarUsuario(RegistrarUsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new DuplicateResourceException("Usuário já cadastrado");
        }

        TipoPerfil tipoPerfil = tipoPerfilRepository.findByNomePerfil(usuarioDTO.nomePerfil())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de perfil não encontrado."));

        Usuario usuario = new Usuario();

        usuario.setNome(usuarioDTO.nome());
        usuario.setEmail(usuarioDTO.email());
        usuario.setSenha(usuarioDTO.senha()); // temporário; depois vamos aplicar hash
        usuario.setCargo(usuarioDTO.cargo());
        usuario.setDepartamento(usuarioDTO.departamento());
        usuario.setTipoPerfil(tipoPerfil);

        usuario.setPontos(0);
        usuario.setStatus(1);
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setDataAtualizacao(LocalDateTime.now());

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
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        return new MostrarUsuarioDTO(usuario);
    }

    public MostrarUsuarioDTO atualizarUsuario(Long id, AtualizarUsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!usuario.getEmail().equals(usuarioDTO.email())
                && usuarioRepository.existsByEmail(usuarioDTO.email())) {
            throw new RuntimeException("Email já cadastrado.");
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
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }

        usuarioRepository.deleteById(id);
    }
}
