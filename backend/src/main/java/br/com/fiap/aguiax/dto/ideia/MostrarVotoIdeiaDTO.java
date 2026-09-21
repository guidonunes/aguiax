package br.com.fiap.aguiax.dto.ideia;

import br.com.fiap.aguiax.model.VotoIdeia;

import java.time.LocalDateTime;

public record MostrarVotoIdeiaDTO(

        Long id,

        Long usuarioId,
        String nomeUsuario,
        String emailUsuario,

        Long ideiaId,
        String tituloIdeia,

        Integer valor,
        LocalDateTime dataVoto

) {
    public MostrarVotoIdeiaDTO(VotoIdeia votoIdeia) {
        this(
                votoIdeia.getId(),

                votoIdeia.getUsuario().getId(),
                votoIdeia.getUsuario().getNome(),
                votoIdeia.getUsuario().getEmail(),

                votoIdeia.getIdeia().getId(),
                votoIdeia.getIdeia().getTitulo(),

                votoIdeia.getValor(),
                votoIdeia.getDataVoto()
        );
    }
}