package br.com.fiap.aguiaexpress.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_orientacoes_estrategicas")
public class OrientacaoEstrategica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orientacao")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "ds_titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "ds_descricao", nullable = false, length = 1000)
    private String descricao;

    @Column(name = "nm_area_estrategica", length = 100)
    private String areaEstrategica;

    @Column(name = "st_status")
    private Integer status;

    @Column(name = "dt_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "dt_data_atualizacao")
    private LocalDateTime dataAtualizacao;
}