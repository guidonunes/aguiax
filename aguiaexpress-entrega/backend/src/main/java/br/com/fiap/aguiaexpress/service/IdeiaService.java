package br.com.fiap.aguiaexpress.service;

import br.com.fiap.aguiaexpress.dto.AtualizarStatusIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.MostrarIdeiaDTO;
import br.com.fiap.aguiaexpress.dto.RegistrarIdeiaDTO;
import br.com.fiap.aguiaexpress.exception.DuplicateResourceException;
import br.com.fiap.aguiaexpress.exception.ResourceNotFoundException;
import br.com.fiap.aguiaexpress.model.*;
import br.com.fiap.aguiaexpress.repository.IdeiaRepository;
import br.com.fiap.aguiaexpress.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IdeiaService {

    private final IdeiaRepository ideiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final StatusIdeiaService statusIdeiaService;
    private final OrientacaoEstrategicaService orientacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public MostrarIdeiaDTO criarIdeia(RegistrarIdeiaDTO ideiaDTO) {
        if (ideiaRepository.existsByTitulo(ideiaDTO.titulo())) {
            throw new DuplicateResourceException("Já existe uma ideia cadastrada com esse título.");
        }

        Usuario usuario = usuarioAutenticadoService.getUsuarioAutenticado();

        Categoria categoria = categoriaService.buscarEntidadePorId(ideiaDTO.categoriaId());

        OrientacaoEstrategica orientacao =
                orientacaoService.buscarEntidadePorId(ideiaDTO.orientacaoId());

        StatusIdeia statusInicial =
                statusIdeiaService.buscarEntidadePorStatus(NomeStatusIdeia.NOVA);

        Ideia ideia = new Ideia();

        ideia.setUsuario(usuario);
        ideia.setCategoria(categoria);
        ideia.setOrientacaoEstrategica(orientacao);
        ideia.setStatusIdeia(statusInicial);

        ideia.setTitulo(ideiaDTO.titulo());
        ideia.setDescricao(ideiaDTO.descricao());
        ideia.setProblema(ideiaDTO.problema());
        ideia.setSolucaoProposta(ideiaDTO.solucaoProposta());

        ideia.setImpacto(0);
        ideia.setViabilidade(0);
        ideia.setPrioridade(0);
        ideia.setTotalVotos(0);

        ideia.setDataCriacao(LocalDateTime.now());
        ideia.setDataAtualizacao(LocalDateTime.now());

        Ideia ideiaSalva = ideiaRepository.save(ideia);

        return new MostrarIdeiaDTO(ideiaSalva);
    }

    public List<MostrarIdeiaDTO> listarIdeias() {
        return ideiaRepository.findAll()
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public MostrarIdeiaDTO buscarIdeiaPorId(Long id) {
        return new MostrarIdeiaDTO(buscarEntidadePorId(id));
    }

    public MostrarIdeiaDTO atualizarIdeia(Long id, RegistrarIdeiaDTO ideiaDTO) {
        Ideia ideia = buscarEntidadePorId(id);

        if (!ideia.getTitulo().equals(ideiaDTO.titulo())
                && ideiaRepository.existsByTitulo(ideiaDTO.titulo())) {
            throw new DuplicateResourceException("Já existe uma ideia cadastrada com esse título.");
        }

        Usuario usuario = usuarioAutenticadoService.getUsuarioAutenticado();

        Categoria categoria = categoriaService.buscarEntidadePorId(ideiaDTO.categoriaId());

        OrientacaoEstrategica orientacao =
                orientacaoService.buscarEntidadePorId(ideiaDTO.orientacaoId());

        ideia.setUsuario(usuario);
        ideia.setCategoria(categoria);
        ideia.setOrientacaoEstrategica(orientacao);

        ideia.setTitulo(ideiaDTO.titulo());
        ideia.setDescricao(ideiaDTO.descricao());
        ideia.setProblema(ideiaDTO.problema());
        ideia.setSolucaoProposta(ideiaDTO.solucaoProposta());

        ideia.setDataAtualizacao(LocalDateTime.now());

        Ideia ideiaAtualizada = ideiaRepository.save(ideia);

        return new MostrarIdeiaDTO(ideiaAtualizada);
    }

    public void deletarIdeia(Long id) {
        Ideia ideia = buscarEntidadePorId(id);

        ideiaRepository.delete(ideia);
    }

    public List<MostrarIdeiaDTO> listarIdeiasPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        return ideiaRepository.findByUsuario(usuario)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public List<MostrarIdeiaDTO> listarIdeiasPorCategoria(Long categoriaId) {
        Categoria categoria = categoriaService.buscarEntidadePorId(categoriaId);

        return ideiaRepository.findByCategoria(categoria)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public List<MostrarIdeiaDTO> listarIdeiasPorStatus(NomeStatusIdeia nomeStatusIdeia) {
        StatusIdeia statusIdeia = statusIdeiaService.buscarEntidadePorStatus(nomeStatusIdeia);

        return ideiaRepository.findByStatusIdeia(statusIdeia)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public List<MostrarIdeiaDTO> listarIdeiasPorOrientacao(Long orientacaoId) {
        OrientacaoEstrategica orientacao =
                orientacaoService.buscarEntidadePorId(orientacaoId);

        return ideiaRepository.findByOrientacaoEstrategica(orientacao)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public List<MostrarIdeiaDTO> buscarIdeiasPorTitulo(String titulo) {
        return ideiaRepository.findByTituloContainingIgnoreCase(titulo)
                .stream()
                .map(MostrarIdeiaDTO::new)
                .toList();
    }

    public Ideia buscarEntidadePorId(Long id) {
        return ideiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ideia não encontrada."));
    }

    public MostrarIdeiaDTO atualizarStatusIdeia(Long id, AtualizarStatusIdeiaDTO statusDTO) {
        Ideia ideia = buscarEntidadePorId(id);

        StatusIdeia novoStatus = statusIdeiaService.buscarEntidadePorStatus(statusDTO.status());

        ideia.setStatusIdeia(novoStatus);
        ideia.setDataAtualizacao(LocalDateTime.now());

        Ideia ideiaAtualizada = ideiaRepository.save(ideia);

        return new MostrarIdeiaDTO(ideiaAtualizada);
    }
}